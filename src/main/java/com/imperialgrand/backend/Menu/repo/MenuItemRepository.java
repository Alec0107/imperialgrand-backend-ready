package com.imperialgrand.backend.Menu.repo;

import com.imperialgrand.backend.Menu.entities.MenuItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
    Page<MenuItem> findByCategoryIdAndIsActiveTrueOrderByDisplayOrderAsc(Long categoryId, Pageable pageable);

    Page<MenuItem> findByCategoryIdAndSubcategoryIdAndIsActiveTrueOrderByDisplayOrderAsc(Long categoryId, Long subcategoryId, Pageable pageable);

    Optional<MenuItem> findBySlugAndIsActiveTrue(String slug);

}
