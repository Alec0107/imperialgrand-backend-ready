package com.imperialgrand.backend.reservations;

import com.imperialgrand.backend.reservations.repository.entity.ReservationOccasionItem;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ReservationOccasionItemRepository extends JpaRepository<ReservationOccasionItem, Long> {

        @Query("""
                select oi.occasionItem
                from ReservationOccasionItem oi
                where oi.reservationsV1.reservationId = :reservationId
              """)
        List<String> findOccasionStrings(@Param("reservationId") Integer reservationId);
}
