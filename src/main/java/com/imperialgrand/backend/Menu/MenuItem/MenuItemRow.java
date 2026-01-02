package com.imperialgrand.backend.Menu.MenuItem;

// src/main/java/com/imperialgrand/backend/Menu/dto/MenuItemRow.java

public interface MenuItemRow {
    Long getId();
    String getNameEn();
    Integer getPriceCents();
    Boolean getIsActive();

    Long getCategoryId();
    String getCategoryName();

    Long getSubcategoryId();
    String getSubcategoryName();

    String getImageUrl();
    Integer getDisplayOrder();
}