package com.imperialgrand.backend.reservations.repository;

import com.imperialgrand.backend.reservations.repository.entity.ReservationDietaryItem;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ReservationDietaryItemRepository extends JpaRepository<ReservationDietaryItem, Long> {

    // Pull just the strings you need for the DTO
    @Query("""
            select di.dietaryItem
            from ReservationDietaryItem di
            where di.reservationsV1.reservationId = :reservationId
          """)
    List<String> findDietaryStrings(@Param("reservationId") Integer reservationId);
}