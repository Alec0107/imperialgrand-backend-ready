package com.imperialgrand.backend.Menu.repo;

import com.imperialgrand.backend.Menu.MenuItem.MenuItemAdminView;
import com.imperialgrand.backend.Menu.MenuItem.MenuItemRow;
import com.imperialgrand.backend.Menu.MenuItem.MenuItemRowDTO;
import com.imperialgrand.backend.Menu.entities.MenuItem;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
    Page<MenuItem> findByCategoryIdAndIsActiveTrueOrderByDisplayOrderAsc(Long categoryId, Pageable pageable);

    Page<MenuItem> findByCategoryIdAndSubcategoryIdAndIsActiveTrueOrderByDisplayOrderAsc(Long categoryId, Long subcategoryId, Pageable pageable);

    Optional<MenuItem> findById(Long Id);

    Optional<MenuItem> findBySlugAndIsActiveTrue(String slug);


    // Admin list (optionally filter later)
    Page<MenuItem> findAll(Pageable pageable);

    // (Optional future) filter by subcategory
    Page<MenuItem> findBySubcategoryId(Long subcategoryId, Pageable pageable);




    @Query(value = """
      select
        mi.id                as id,
        mi.name_en           as nameEn,
        mi.price_cents       as priceCents,
        mi.is_active         as isActive,

        c.id                 as categoryId,
        c.name               as categoryName,

        s.id                 as subcategoryId,
        s.name               as subcategoryName,

        mi.image_url         as imageUrl,
        mi.display_order     as displayOrder
      from menu_item mi
      join menu_categories  c on c.id = mi.category_id
      left join menu_subcategory s on s.id = mi.subcategory_id
      order by c.display_order asc,
               s.display_order asc nulls last,
               mi.display_order asc,
               mi.name_en asc
      """,
            countQuery = "select count(*) from menu_item",
            nativeQuery = true)
    Page<MenuItemRow> findRows(Pageable pageable);

    @Query(value = """
      SELECT
        mi.id,
        mi.name_en       AS nameEn,
        mi.name_cn       AS nameCn,
        mi.blurb_en      AS blurbEn,

        mi.category_id   AS categoryId,
        cat.name         AS categoryName,

        mi.subcategory_id AS subcategoryId,
        sub.name          AS subcategoryName,

        mi.price_cents    AS priceCents,
        mi.price_suffix   AS priceSuffix,

        mi.is_active      AS isActive,
        mi.is_signature   AS isSignature,
        mi.is_spicy       AS isSpicy,

        mi.image_url      AS imageUrl,
        mi.display_order  AS displayOrder,
        mi.slug           AS slug
      FROM menu_item mi
      LEFT JOIN menu_categories  cat ON cat.id  = mi.category_id
      LEFT JOIN menu_subcategory sub ON sub.id  = mi.subcategory_id
      WHERE mi.id = :id
      """,
            nativeQuery = true)
    Optional<MenuItemAdminView> findAdminViewById(@Param("id") Long id);


    boolean existsBySlugAndIdNot(String slug, Long id);


}




