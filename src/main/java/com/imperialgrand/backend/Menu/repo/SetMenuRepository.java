package com.imperialgrand.backend.Menu.repo;


import com.imperialgrand.backend.Menu.entities.SetMenu;
import org.springframework.data.repository.query.Param;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface SetMenuRepository extends JpaRepository<SetMenu, Long> {
    Page<SetMenu> findAllByIsActiveTrueOrderByDisplayOrderAsc(Pageable pageable);

    // For admin list page (all, ordered)
    Page<SetMenu> findAllByOrderByDisplayOrderAsc(Pageable pageable);

    // Optional: only active, if you need for public site later
    Page<SetMenu> findByIsActiveTrueOrderByDisplayOrderAsc(Pageable pageable);

    @Modifying
    @Transactional
    @Query(
            value = """
            UPDATE set_menu
            SET courses_json = CAST(:json AS jsonb)
            WHERE id = :id
            """,
            nativeQuery = true
    )
    void updateCoursesJson(@Param("id") Long id, @Param("json") String json);

    boolean existsBySlugAndIdNot(String slug, Long id);

}


