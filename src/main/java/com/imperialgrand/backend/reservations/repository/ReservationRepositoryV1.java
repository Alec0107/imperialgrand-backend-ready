package com.imperialgrand.backend.reservations.repository;

import com.imperialgrand.backend.Admin.ReservationListItemDTO;
import com.imperialgrand.backend.authentication.DTO.User;
import com.imperialgrand.backend.reservations.Tables.ReservationStatus;
import com.imperialgrand.backend.reservations.repository.entity.ReservationsV1;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepositoryV1 extends JpaRepository<ReservationsV1, Long> {


    @Query("""
            SELECT COUNT(r) > 0
            FROM reservation r
            WHERE r.table.id = :tableId
            AND r.startTime < :endTime
            AND r.endTime > :startTime
            AND r.status IN :statuses
            """)
    boolean existsOverlap(@Param("tableId")   long tableId,
                          @Param("startTime") LocalDateTime startTime,
                          @Param("endTime")   LocalDateTime endTime,
                          @Param("statuses")  List<ReservationStatus> statuses);

    Page<ReservationsV1> findByUserAndStatusNotAndStartTimeAfter(
            User user,
            ReservationStatus status,
            LocalDateTime startTime,
            Pageable pageable
    );

    Page<ReservationsV1> findByUserAndStatusNotAndStartTimeBefore(
            User user,
            ReservationStatus status,
            LocalDateTime startTime,
            Pageable pageable
    );

    // Get the base reservation (+ table) and if needed, assert ownership by userId
    @Query("""
    select r from reservation r
      join fetch r.table t
    where r.reservationId = :id
      and (:userId is null or r.user.id = :userId)
  """)
    Optional<ReservationsV1> findBaseWithTable(@Param("id") Integer id,
                                               @Param("userId") Long userId);

    Page<ReservationsV1> findByStatus(ReservationStatus status, Pageable pageable);

    // Upcoming = future + not cancelled
    @Query("""
         select r from reservation r
           join fetch r.table t
         where r.status = com.imperialgrand.backend.reservations.Tables.ReservationStatus.CONFIRMED
           and r.startTime >= :now
         """)
    Page<ReservationsV1> findUpcoming(@Param("now") LocalDateTime now, Pageable pageable);

    // Past = start time in the past (you can exclude CANCELLED if you want)
    @Query("""
         select r from reservation r
           join fetch r.table t
         where r.startTime < :now
         """)
    Page<ReservationsV1> findPast(@Param("now") LocalDateTime now, Pageable pageable);


    @EntityGraph(attributePaths = {"table", "dietaryItems", "occasionItems"})
    @Query("""
    select r from reservation r
    where r.reservationId = :id
""")
    Optional<ReservationsV1> fetchDetails(@Param("id") Integer id);
}
