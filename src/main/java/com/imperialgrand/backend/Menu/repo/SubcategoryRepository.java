package com.imperialgrand.backend.Menu.repo;

import com.imperialgrand.backend.Menu.entities.SubcategoryDTO;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubcategoryRepository extends JpaRepository<SubcategoryDTO, Long> {

    List<SubcategoryDTO> findByCategoryIdInOrderByDisplayOrderAsc(List<Long> categoryIds);
    List<SubcategoryDTO> findByCategoryIdOrderByDisplayOrder(Long categoryId);

    // fetch subcategories for one category with sorting
    List<SubcategoryDTO> findByCategoryId(Long categoryId, Sort sort);

    // cascade delete when a category is removed
    void deleteAllByCategoryId(Long categoryId);
}





