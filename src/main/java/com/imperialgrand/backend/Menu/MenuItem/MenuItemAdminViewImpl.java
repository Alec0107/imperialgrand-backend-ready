package com.imperialgrand.backend.Menu.MenuItem;

// package com.imperialgrand.backend.Menu.MenuItem;  <-- match your package


public record MenuItemAdminViewImpl(
        Long id,
        String nameEn,
        String nameCn,
        String blurbEn,
        String slug,
        Long categoryId,
        String categoryName,
        Long subcategoryId,
        String subcategoryName,
        Integer priceCents,
        String priceSuffix,
        String imageUrl,
        Boolean isSignature,
        Boolean isSpicy,
        Boolean isActive,
        Integer displayOrder
) implements MenuItemAdminView {
    @Override
    public Long getId() {
        return 0L;
    }

    @Override
    public String getNameEn() {
        return "";
    }

    @Override
    public String getNameCn() {
        return "";
    }

    @Override
    public String getBlurbEn() {
        return "";
    }

    @Override
    public Long getCategoryId() {
        return 0L;
    }

    @Override
    public String getCategoryName() {
        return "";
    }

    @Override
    public Long getSubcategoryId() {
        return 0L;
    }

    @Override
    public String getSubcategoryName() {
        return "";
    }

    @Override
    public Integer getPriceCents() {
        return 0;
    }

    @Override
    public String getPriceSuffix() {
        return "";
    }

    @Override
    public Boolean getIsActive() {
        return null;
    }

    @Override
    public Boolean getIsSignature() {
        return null;
    }

    @Override
    public Boolean getIsSpicy() {
        return null;
    }

    @Override
    public String getImageUrl() {
        return "";
    }

    @Override
    public Integer getDisplayOrder() {
        return 0;
    }

    @Override
    public String getSlug() {
        return "";
    }
}