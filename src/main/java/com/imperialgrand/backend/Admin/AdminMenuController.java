package com.imperialgrand.backend.Admin;



import com.imperialgrand.backend.Menu.MenuItem.SetMenuServiceAdmin;
import com.imperialgrand.backend.Menu.entities.CategoryDTO;
import com.imperialgrand.backend.Menu.entities.SubcategoryDTO;
import com.imperialgrand.backend.Menu.repo.CategoryRepository;
import com.imperialgrand.backend.Menu.repo.SubcategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.data.domain.Sort;

import java.text.Normalizer;
import java.util.List;
import java.util.logging.Logger;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/admin/dashboard")
public class AdminMenuController {
    private final CategoryRepository categoryRepo;
    private final SubcategoryRepository subRepo;

    private final SetMenuServiceAdmin setMenuService;
    // etc...

    private final Logger logger = Logger.getLogger(AdminMenuController.class.getName());

    /* ====================== CATEGORIES ====================== */

    // GET /api/auth/admin/menu-categories
    @GetMapping("/menu-categories")
    public List<CategoryView> listCategories() {
        Sort sort = Sort.by(Sort.Order.asc("displayOrder"), Sort.Order.asc("name"));
        return categoryRepo.findAll(sort).stream()
                .map(c -> new CategoryView(c.getId(), c.getName(), c.getDisplayOrder()))
                .toList();
    }

    // POST /api/auth/admin/menu-categories
    @PostMapping("/menu-categories")
    public CategoryView createCategory(@RequestBody CreateCategoryReq req) {
        if (req == null || isBlank(req.name())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name is required");
        }
        CategoryDTO c = new CategoryDTO();
        c.setName(req.name().trim());
        c.setSlug(slugify(req.name()));
        c.setDisplayOrder(req.displayOrder());
        c = categoryRepo.save(c);
        return new CategoryView(c.getId(), c.getName(), c.getDisplayOrder());
    }

    // PATCH /api/auth/admin/menu-categories/{id}
    @PatchMapping("/menu-categories/{id}")
    public CategoryView updateCategory(@PathVariable Long id, @RequestBody UpdateCategoryReq req) {
        CategoryDTO c = categoryRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));

        if (req.name() != null) {
            String name = req.name().trim();
            if (isBlank(name)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name cannot be blank");
            c.setName(name);
            c.setSlug(slugify(name));
        }
        if (req.displayOrder() != null) c.setDisplayOrder(req.displayOrder());

        c = categoryRepo.save(c);
        return new CategoryView(c.getId(), c.getName(), c.getDisplayOrder());
    }

    // DELETE /api/auth/admin/menu-categories/{id}
    @DeleteMapping("/menu-categories/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        if (!categoryRepo.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found");
        }
        subRepo.deleteAllByCategoryId(id); // manual cascade
        categoryRepo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    /* ====================== SUBCATEGORIES ====================== */

    // GET /api/auth/admin/menu-subcategories?categoryId=123
    @GetMapping("/menu-subcategories")
    public List<SubcategoryView> listSubcategories(@RequestParam Long categoryId) {
        if (!categoryRepo.existsById(categoryId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found");
        }
        Sort sort = Sort.by(Sort.Order.asc("displayOrder"), Sort.Order.asc("name"));
        return subRepo.findByCategoryId(categoryId, sort).stream()
                .map(s -> new SubcategoryView(s.getId(), s.getName(), s.getDisplayOrder()))
                .toList();
    }

    // POST /api/auth/admin/menu-subcategories?categoryId=123
    @PostMapping("/menu-subcategories")
    public SubcategoryView createSubcategory(@RequestParam Long categoryId, @RequestBody CreateSubcategoryReq req) {
        if (!categoryRepo.existsById(categoryId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found");
        }
        if (req == null || isBlank(req.name())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name is required");
        }
        SubcategoryDTO s = new SubcategoryDTO();
        s.setCategoryId(categoryId);
        s.setName(req.name().trim());
        s.setSlug(slugify(req.name()));
        s.setDisplayOrder(req.displayOrder());
        s = subRepo.save(s);
        return new SubcategoryView(s.getId(), s.getName(), s.getDisplayOrder());
    }

    // PATCH /api/auth/admin/menu-subcategories/{id}
    @PatchMapping("/menu-subcategories/{id}")
    public SubcategoryView updateSubcategory(@PathVariable Long id, @RequestBody UpdateSubcategoryReq req) {
        SubcategoryDTO s = subRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Subcategory not found"));

        if (req.name() != null) {
            String name = req.name().trim();
            if (isBlank(name)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name cannot be blank");
            s.setName(name);
            s.setSlug(slugify(name));
        }
        if (req.displayOrder() != null) s.setDisplayOrder(req.displayOrder());

        s = subRepo.save(s);
        return new SubcategoryView(s.getId(), s.getName(), s.getDisplayOrder());
    }

    // DELETE /api/auth/admin/menu-subcategories/{id}
    @DeleteMapping("/menu-subcategories/{id}")
    public ResponseEntity<Void> deleteSubcategory(@PathVariable Long id) {
        if (!subRepo.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Subcategory not found");
        }
        subRepo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    /* ====================== DTOs (API shapes) ====================== */
    public record CategoryView(Long id, String name, Integer displayOrder) {}
    public record CreateCategoryReq(String name, Integer displayOrder) {}
    public record UpdateCategoryReq(String name, Integer displayOrder) {}

    public record SubcategoryView(Long id, String name, Integer displayOrder) {}
    public record CreateSubcategoryReq(String name, Integer displayOrder) {}
    public record UpdateSubcategoryReq(String name, Integer displayOrder) {}

    /* ====================== helpers ====================== */
    private static boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }

    private static String slugify(String input) {
        String n = Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        n = n.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
        return n.isEmpty() ? "item" : n;
    }






    // ===== SET MENUS (admin) =====

    @GetMapping("/set-menus")
    public Page<SetMenuServiceAdmin.SetMenuView> listSetMenus(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return setMenuService.list(page, size);
    }

    @GetMapping("/set-menus/{id}")
    public SetMenuServiceAdmin.SetMenuView getSetMenu(@PathVariable long id) {
        return setMenuService.getOne(id);
    }

    @PostMapping("/set-menus")
    @ResponseStatus(HttpStatus.CREATED)
    public SetMenuServiceAdmin.SetMenuView createSetMenu(
            @RequestBody SetMenuServiceAdmin.CreateSetMenuReq req
    ) {
        logger.info(String.valueOf(req.toString()));
        return setMenuService.create(req);
    }

    @PatchMapping("/set-menus/{id}")
    public SetMenuServiceAdmin.SetMenuView updateSetMenu(
            @PathVariable long id,
            @RequestBody SetMenuServiceAdmin.UpdateSetMenuReq req
    ) {

        return setMenuService.update(id, req);
    }

    @DeleteMapping("/set-menus/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSetMenu(@PathVariable long id) {
        setMenuService.delete(id);
    }




}

