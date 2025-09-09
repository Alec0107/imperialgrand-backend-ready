package com.imperialgrand.backend.Menu.entities;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "menu_item")
@Data
public class MenuItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // links to your category + subcategory
    @Column(nullable = false)
    private Long categoryId;

    private Long subcategoryId; // nullable (e.g. set menu or desserts may not need)

    // basic info
    @Column(nullable = false)
    private String nameEn;

    private String nameCn;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(name = "blurb_en")       // optional; naming strategy already does this
    private String blurbEn;


    // price & display
    private Integer priceCents;        // e.g. 880 = $8.80
    private String priceSuffix;        // e.g. "++" or "per pax"

    private String imageUrl;

    private Boolean isSignature = false;  // optional flag (like chef’s special)
    private Boolean isSpicy = false;

    private Boolean isActive = true;

    private Integer displayOrder = 0;
}
