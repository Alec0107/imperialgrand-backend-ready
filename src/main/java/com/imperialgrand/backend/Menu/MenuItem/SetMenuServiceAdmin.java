package com.imperialgrand.backend.Menu.MenuItem;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.imperialgrand.backend.Menu.entities.SetMenu;
import com.imperialgrand.backend.Menu.repo.SetMenuRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

import org.springframework.data.domain.*;



import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.imperialgrand.backend.Menu.entities.SetMenu;
import com.imperialgrand.backend.Menu.repo.SetMenuRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Service
@RequiredArgsConstructor
public class SetMenuServiceAdmin {

    private final SetMenuRepository repo;
    private final ObjectMapper mapper;   // you already have ObjectMapper bean

    // ===== DTOs =====

    // what admin frontend sees
    public record SetMenuView(
            Long id,
            String nameEn,
            String nameCn,
            String slug,
            Integer pax,
            Integer priceCents,
            String priceSuffix,
            String imageUrl,
            String description,
            Integer displayOrder,
            Boolean isActive,
            List<String> dishes     // 👈 array of strings
    ) {}

    // POST body (create)
    public record CreateSetMenuReq(
            String nameEn,
            String nameCn,
            String slug,
            Integer pax,
            Integer priceCents,
            String priceSuffix,
            String imageUrl,
            String description,
            Integer displayOrder,
            Boolean isActive,
            List<String> dishes     // 👈 array of strings
    ) {}

    // PATCH body (update)
    public record UpdateSetMenuReq(
            String nameEn,
            String nameCn,
            String slug,
            Integer pax,
            Integer priceCents,
            String priceSuffix,
            String imageUrl,
            String description,
            Integer displayOrder,
            Boolean isActive,
            List<String> dishes     // 👈 array of strings
    ) {}

    // ===== PUBLIC METHODS =====

    public Page<SetMenuView> list(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<SetMenu> p = repo.findAllByOrderByDisplayOrderAsc(pageable);

        List<SetMenuView> views = p.getContent()
                .stream()
                .map(this::toView)
                .toList();

        return new PageImpl<>(views, pageable, p.getTotalElements());
    }

    public SetMenuView getOne(long id) {
        SetMenu m = repo.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Set menu not found"));
        return toView(m);
    }

    @Transactional
    public SetMenuView create(CreateSetMenuReq r) {
        if (r == null || r.nameEn() == null || r.nameEn().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name is required");
        }
        if (r.priceCents() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Price is required");
        }
        if (r.pax() == null || r.pax() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Pax is required");
        }

        // 1) save all scalar fields (no coursesJson)
        SetMenu m = new SetMenu();
        m.setNameEn(r.nameEn().trim());
        m.setNameCn(Objects.toString(r.nameCn(), "").trim());
        m.setPax(r.pax());
        m.setBlurbEn(Objects.toString(r.description(), "").trim());
        m.setBasePriceCents(r.priceCents());
        m.setPriceSuffix(Objects.toString(r.priceSuffix(), "").trim());
        m.setImageUrl(blankToNull(r.imageUrl()));
        m.setDisplayOrder(r.displayOrder() != null ? r.displayOrder() : 999);
        m.setIsActive(r.isActive() != null ? r.isActive() : Boolean.TRUE);

        // slug
        String slug = Objects.toString(r.slug(), "").trim();
        if (slug.isBlank()) {
            slug = slugify(m.getNameEn());
        }
        slug = ensureUniqueSlug(slug, null);
        m.setSlug(slug);

        // ❌ do NOT call m.setCoursesJson(...)

        m = repo.save(m);   // insert without courses_json

        // 2) now update jsonb column if dishes are provided
        if (r.dishes() != null && !r.dishes().isEmpty()) {
            try {
                // example: ["北京片皮鴨 / Peking Duck", "紅燒干貝鮮魚鰾翅 / Braised Soup", ...]
                String json = mapper.writeValueAsString(r.dishes());
                repo.updateCoursesJson(m.getId(), json);
            } catch (JsonProcessingException e) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Invalid dishes JSON", e
                );
            }
        }

        SetMenu saved = repo.findById(m.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR, "Saved set menu missing"));

        return toView(saved);
    }

    @Transactional
    public SetMenuView update(long id, UpdateSetMenuReq r) {
        SetMenu m = repo.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Set menu not found"));

        if (r.nameEn() != null)        m.setNameEn(r.nameEn().trim());
        if (r.nameCn() != null)        m.setNameCn(r.nameCn().trim());
        if (r.pax() != null)           m.setPax(r.pax());
        if (r.priceCents() != null)    m.setBasePriceCents(r.priceCents());
        if (r.priceSuffix() != null)   m.setPriceSuffix(r.priceSuffix());
        if (r.imageUrl() != null)      m.setImageUrl(blankToNull(r.imageUrl()));
        if (r.description() != null)   m.setBlurbEn(r.description());
        if (r.displayOrder() != null)  m.setDisplayOrder(r.displayOrder());
        if (r.isActive() != null)      m.setIsActive(r.isActive());

        if (r.slug() != null && !r.slug().trim().isEmpty()) {
            String slug = ensureUniqueSlug(r.slug().trim(), m.getId());
            m.setSlug(slug);
        }

        repo.save(m);   // update scalar fields

        // update jsonb courses_json
        if (r.dishes() != null) {
            try {
                String json = mapper.writeValueAsString(r.dishes());
                repo.updateCoursesJson(m.getId(), json);
            } catch (JsonProcessingException e) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Invalid dishes JSON", e
                );
            }
        }

        SetMenu updated = repo.findById(m.getId()).orElseThrow();
        return toView(updated);
    }

    public void delete(long id) {
        if (!repo.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Set menu not found");
        }
        repo.deleteById(id);
    }

    // ===== HELPERS =====

    private SetMenuView toView(SetMenu m) {
        List<String> dishes = deserializeDishes(m.getCoursesJson());

        return new SetMenuView(
                m.getId(),
                m.getNameEn(),
                m.getNameCn(),
                m.getSlug(),
                m.getPax(),
                m.getBasePriceCents(),
                m.getPriceSuffix(),
                m.getImageUrl(),
                m.getBlurbEn(),
                m.getDisplayOrder(),
                Boolean.TRUE.equals(m.getIsActive()),
                dishes
        );
    }

    private String blankToNull(String s) {
        return (s == null || s.trim().isBlank()) ? null : s.trim();
    }

    private String slugify(String s) {
        if (s == null) return null;
        return s.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9\\s-]", "")
                .trim()
                .replaceAll("\\s+", "-");
    }

    private String ensureUniqueSlug(String base, Long excludeId) {
        String s = base;
        long n = 1;
        Long ex = (excludeId == null ? -1L : excludeId);
        while (repo.existsBySlugAndIdNot(s, ex)) {
            s = base + "-" + (++n);
        }
        return s;
    }

    // read courses_json → List<String>
    private List<String> deserializeDishes(String json) {
        if (json == null || json.isBlank()) return List.of();
        try {
            return mapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return List.of(); // if old data bad, just show nothing
        }
    }
}