package com.imperialgrand.backend.Menu.repo;


import com.imperialgrand.backend.Menu.entities.SetMenu;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SetMenuRepository extends JpaRepository<SetMenu, Long> {
    Page<SetMenu> findAllByIsActiveTrueOrderByDisplayOrderAsc(Pageable pageable);
}
