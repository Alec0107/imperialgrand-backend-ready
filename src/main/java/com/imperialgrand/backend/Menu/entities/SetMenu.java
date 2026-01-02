package com.imperialgrand.backend.Menu.entities;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "set_menu")
@Data
public class SetMenu {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(name = "name_en", nullable = false)
        private String nameEn;

        @Column(name = "name_cn")
        private String nameCn;

        @Column(nullable = false, unique = true)
        private String slug;

        @Column(nullable = false)
        private Integer pax;

        @Column(name = "base_price_cents", nullable = false)
        private Integer basePriceCents;

        @Column(name = "price_suffix")
        private String priceSuffix;

        @Column(name = "image_url")
        private String imageUrl;

        @Column(name = "blurb_en")
        private String blurbEn;

        @Column(name = "blurb_cn")
        private String blurbCn;

    // other imports/annotations stay the same

    @Column(
            name = "courses_json",
            columnDefinition = "jsonb",
            insertable = false,
            updatable = false
    )
    private String coursesJson;

        @Column(name = "display_order", nullable = false)
        private Integer displayOrder = 999;

        @Column(name = "is_active", nullable = false)
        private Boolean isActive = true;
    }