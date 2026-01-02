package com.imperialgrand.backend.Menu.MenuItem;

public record MenuItemRowDTO(
        Long id,
        String nameEn,
        Integer priceCents,
        Boolean isActive,
        Long categoryId,
        String categoryName,
        Long subcategoryId,      // can be null
        String subcategoryName,  // can be null
        String imageUrl,
        Integer displayOrder
) {}