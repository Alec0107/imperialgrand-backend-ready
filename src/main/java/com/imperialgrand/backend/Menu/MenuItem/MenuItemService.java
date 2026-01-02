package com.imperialgrand.backend.Menu.MenuItem;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.imperialgrand.backend.Menu.Exception.MenuItemNotFoundExcetion;
import com.imperialgrand.backend.Menu.entities.CategoryDTO;
import com.imperialgrand.backend.Menu.entities.MenuItem;
import com.imperialgrand.backend.Menu.entities.SubcategoryDTO;
import com.imperialgrand.backend.Menu.repo.CategoryRepository;
import com.imperialgrand.backend.Menu.repo.MenuItemRepository;
import com.imperialgrand.backend.Menu.repo.SubcategoryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.awt.*;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class MenuItemService {

    private final MenuItemRepository menuItemRepository;
    private final CategoryRepository categoryRepo;
    private final SubcategoryRepository subcategoryRepo;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper mapper = new ObjectMapper();

    public Page<MenuItem> menuItemLists(Long categoryId, Long subcategoryId, int page, int size) {
        var pageable = PageRequest.of(page, size);
        Page<MenuItem> menuItemResult = null;
        String menuItemRedisKey = "";

        if(subcategoryId != null) {
            menuItemRedisKey = CatAndSubKey(categoryId, subcategoryId, page, size);    //Ig:Cat-1:Sub-2:Page-1:Size-1
            String menuItemCached = getRedisString(menuItemRedisKey);

            if(menuItemCached != null){
                menuItemResult = convertStringToObject(menuItemCached);
            }else{
                System.out.println("CACHE MISS! Fetching Cat and Sub from database");
                menuItemResult = menuItemRepository.findByCategoryIdAndSubcategoryIdAndIsActiveTrueOrderByDisplayOrderAsc(categoryId, subcategoryId, pageable);
            }

        }else{
            menuItemRedisKey = CatKey(categoryId, page, size);
            String menuItemCached = getRedisString(menuItemRedisKey);

            if(menuItemCached != null){

            }else{
                menuItemResult = menuItemRepository.findByCategoryIdAndIsActiveTrueOrderByDisplayOrderAsc(categoryId, pageable);
            }


        }
        return menuItemResult;
    }

    private Page<MenuItem> convertStringToObject(String cached){
        Page<MenuItem> redisResult = null;
        try{
            System.out.println("CACHE HIT! Fetching Cat and Sub from redis");
            redisResult =  mapper.readValue(cached, new TypeReference<Page<MenuItem>>() {});
        }catch(JsonProcessingException e){
            System.out.println("Menu Item: Failed to fetch from redis: " + e.getMessage());
        }
        return redisResult;
    }

    private String getRedisString(String key){
        return redisTemplate.opsForValue().get(key);
    }

    public MenuItem getMenuItemById(Long id){
       return menuItemRepository.findById(id)
                .orElseThrow(() -> new MenuItemNotFoundExcetion("Menu Item Not Found"));
    }

    private String CatAndSubKey(Long categoryId, Long subcategoryId, int page, int size){
        return "ig:Cat-" + categoryId + ":Sub-" + subcategoryId + ":Page-" + page + ":Size-" + size;
    }

    private String CatKey(Long categoryId, int page, int size){
        return "ig:Cat-" + categoryId + ":Page-" + page + ":Size-" + size;
    }

    public Page<MenuItemRow> findRows(Pageable pageable){
       return menuItemRepository.findRows(pageable);
    }

    public MenuItemAdminView getAdminView(long id) {
        return menuItemRepository.findAdminViewById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Menu item not found"));
    }

    public MenuItemAdminView update(long id, UpdateMenuItemReq r) {
        MenuItem m = menuItemRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found"));

        // 1) Basic fields (apply only if provided)
        if (r.nameEn()       != null) m.setNameEn(r.nameEn().trim());
        if (r.nameCn()       != null) m.setNameCn(r.nameCn().trim());
        if (r.blurbEn()      != null) m.setBlurbEn(r.blurbEn().trim());
        if (r.priceCents()   != null) m.setPriceCents(r.priceCents());
        if (r.priceSuffix()  != null) m.setPriceSuffix(r.priceSuffix().trim());
        if (r.isActive()     != null) m.setIsActive(r.isActive());
        if (r.isSignature()  != null) m.setIsSignature(r.isSignature());
        if (r.isSpicy()      != null) m.setIsSpicy(r.isSpicy());
        if (r.imageUrl()     != null) m.setImageUrl(r.imageUrl().isBlank() ? null : r.imageUrl().trim());
        if (r.displayOrder() != null) m.setDisplayOrder(r.displayOrder());

        // 2) Category / Subcategory (validate)
        if (r.categoryId() != null) {
            if (!categoryRepo.existsById(r.categoryId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category not found");
            }
            m.setCategoryId(r.categoryId());
        }

        if (r.subcategoryId() != null) {
            // allow null to clear
            if (r.subcategoryId() == 0) {
                m.setSubcategoryId(null);
            } else {
                // ensure subcat exists and (if category provided) belongs to that category
                var sub = subcategoryRepo.findById(r.subcategoryId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Subcategory not found"));
                Long catId = (r.categoryId() != null) ? r.categoryId() : m.getCategoryId();
                if (!sub.getCategoryId().equals(catId)) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Subcategory doesn't belong to category");
                }
                m.setSubcategoryId(sub.getId());
            }
        }
        // 3) Slug (keep unique; regenerate if blank)
        if (r.slug() != null) {
            String s = r.slug().trim();
            if (s.isBlank()) {
                s = slugify(m.getNameEn());
            }
            if (menuItemRepository.existsBySlugAndIdNot(s, m.getId())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Slug already in use");
            }
            m.setSlug(s);
        }
        menuItemRepository.save(m);
        return toAdminView(m);
    }

    private String slugify(String s) {
        if (s == null) return null;
        return s.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .trim()
                .replaceAll("\\s+", "-");
    }

    private MenuItemAdminView toAdminView(MenuItem m) {
        String categoryName = categoryRepo.findById(m.getCategoryId())
                .map(CategoryDTO::getName).orElse(null);
        String subcatName = (m.getSubcategoryId() == null) ? null :
                subcategoryRepo.findById(m.getSubcategoryId()).map(SubcategoryDTO::getName).orElse(null);

        return new MenuItemAdminViewImpl(
                m.getId(),
                m.getNameEn(), m.getNameCn(), m.getBlurbEn(),
                m.getSlug(),
                m.getCategoryId(), categoryName,
                m.getSubcategoryId(), subcatName,
                m.getPriceCents(), m.getPriceSuffix(),
                m.getImageUrl(),
                Boolean.TRUE.equals(m.getIsSignature()),
                Boolean.TRUE.equals(m.getIsSpicy()),
                Boolean.TRUE.equals(m.getIsActive()),
                m.getDisplayOrder()
        );
    }

    @Transactional
    public MenuItemAdminView create(CreateMenuItemReq r) {
        // 1) validate category/subcategory like in update()
        if (r.categoryId() == null || !categoryRepo.existsById(r.categoryId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category not found");
        }
        Long subId = r.subcategoryId();
        if (subId != null) {
            var sub = subcategoryRepo.findById(subId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Subcategory not found"));
            if (!Objects.equals(sub.getCategoryId(), r.categoryId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Subcategory doesn't belong to category");
            }
        }

        // 2) entity
        var m = new MenuItem();
        m.setNameEn(Objects.toString(r.nameEn(), "").trim());
        m.setNameCn(Objects.toString(r.nameCn(), "").trim());
        m.setBlurbEn(Objects.toString(r.blurbEn(), "").trim());
        m.setCategoryId(r.categoryId());
        m.setSubcategoryId(r.subcategoryId());
        m.setPriceCents(r.priceCents());
        m.setPriceSuffix(Objects.toString(r.priceSuffix(), "").trim());
        m.setIsActive(Boolean.TRUE.equals(r.isActive()));
        m.setIsSignature(Boolean.TRUE.equals(r.isSignature()));
        m.setIsSpicy(Boolean.TRUE.equals(r.isSpicy()));
        m.setImageUrl(blankToNull(r.imageUrl()));
        m.setDisplayOrder(r.displayOrder());

        // 3) slug (generate if blank, ensure unique)
        String slug = Objects.toString(r.slug(), "").trim();
        if (slug.isBlank()) slug = slugify(m.getNameEn());
        slug = ensureUniqueSlug(slug, null); // pass null for new item
        m.setSlug(slug);

        // 4) save
        m = menuItemRepository.save(m);

        // 5) return admin view (reuse your toAdminView)
        return toAdminView(m);
    }

    private String blankToNull(String s){ return (s==null || s.trim().isBlank())? null : s.trim(); }

    private String ensureUniqueSlug(String base, Long excludeId){
        String s = base;
        int n = 1;
        while (menuItemRepository.existsBySlugAndIdNot(s, excludeId == null ? -1L : excludeId)) {
            s = base + "-" + (++n);
        }
        return s;
    }


}

