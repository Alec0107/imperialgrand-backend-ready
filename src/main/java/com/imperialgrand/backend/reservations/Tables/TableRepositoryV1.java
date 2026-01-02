package com.imperialgrand.backend.reservations.Tables;

import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TableRepositoryV1 extends JpaRepository<Tables, Long> {

    @Query("""
        SELECT t
        FROM restaurant_table t
        WHERE t.isActive = true
          AND t.capacity >= :partySize
        ORDER BY t.capacity ASC
    """)
    List<Tables> findCandidateTables(@Param("partySize") int partySize);


}


