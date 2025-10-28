package com.imperialgrand.backend.Menu.entities;

import lombok.*;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
public class CategoryTreeDTO {

    private Long catId;
    private String catName;
    private String catSlug;
    private List<SubcategoryDTO> subcategories = new ArrayList<>();

    public CategoryTreeDTO(Long catId, String catName, String catSlug) {
        this.catId = catId;
        this.catName = catName;
        this.catSlug = catSlug;
    }
}
