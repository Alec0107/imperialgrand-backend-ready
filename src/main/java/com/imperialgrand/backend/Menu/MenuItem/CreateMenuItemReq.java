package com.imperialgrand.backend.Menu.MenuItem;

public record CreateMenuItemReq(
        String nameEn,
        String nameCn,
        String blurbEn,
        Long   categoryId,
        Long   subcategoryId,   // nullable
        Integer priceCents,
        String  priceSuffix,
        Boolean isActive,
        Boolean isSignature,
        Boolean isSpicy,
        String  imageUrl,
        Integer displayOrder,
        String  slug            // optional; if blank, service generates
) {}