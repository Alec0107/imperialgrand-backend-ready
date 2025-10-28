package com.imperialgrand.backend.Menu.entities;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity

@Table(name = "menu_subcategory")
public class SubcategoryDTO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false) private String name;
    @Column(nullable = false, unique = true) private String slug;
    @Column(name="display_order" , nullable = false) private Integer displayOrder;
    @Column(name = "category_id", nullable = false) private Long categoryId;

    public SubcategoryDTO(Long id, String name, String slug, Integer displayOrder) {
        this.id = id;
        this.name = name;
        this.slug = slug;
        this.displayOrder = displayOrder;
    }

    public SubcategoryDTO() {

    }
}