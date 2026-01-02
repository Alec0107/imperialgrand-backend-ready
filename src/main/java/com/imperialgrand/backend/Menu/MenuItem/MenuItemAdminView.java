package com.imperialgrand.backend.Menu.MenuItem;



public interface MenuItemAdminView {
    Long getId();
    String getNameEn();
    String getNameCn();
    String getBlurbEn();

    Long getCategoryId();
    String getCategoryName();

    Long getSubcategoryId();
    String getSubcategoryName();

    Integer getPriceCents();
    String getPriceSuffix();

    Boolean getIsActive();
    Boolean getIsSignature();
    Boolean getIsSpicy();

    String getImageUrl();
    Integer getDisplayOrder();
    String getSlug();
}