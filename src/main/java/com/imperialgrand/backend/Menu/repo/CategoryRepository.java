package com.imperialgrand.backend.Menu.repo;

import com.imperialgrand.backend.Menu.entities.CategoryDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<CategoryDTO, Long> {

    List<CategoryDTO> findAllByOrderByDisplayOrderAsc();

}
