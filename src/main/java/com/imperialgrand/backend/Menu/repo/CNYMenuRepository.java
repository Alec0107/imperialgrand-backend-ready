package com.imperialgrand.backend.Menu.repo;

import com.imperialgrand.backend.Menu.entities.CNYMenu;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CNYMenuRepository  extends JpaRepository<CNYMenu, Long> {
    Page<CNYMenu> findByCategoryAndIsActiveTrue(String category, Pageable pageable);
}
