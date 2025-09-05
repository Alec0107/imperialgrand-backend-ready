package com.imperialgrand.backend.reservation.reservation_repository;

import com.imperialgrand.backend.reservation.table_entity.Reservation;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Integer> {

    @Query(value = "SELECT table_id FROM reservations WHERE date = :date AND time BETWEEN :startTime AND :endTime", nativeQuery = true)
    List<Integer> findBookedTableId(@Param("date")LocalDate date,
                                    @Param("startTime") LocalTime startTime,
                                    @Param("endTime") LocalTime endTime);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO reservations (date, time, guest_count, first_name, last_name, email, phone_number, special_request, status, saved_at, table_id, user_id) " +
                   "VALUES (:date, :time, :guest_count, :first_name, :last_name, :email, :phone_number, :special_request, :status, :saved_at, :table_id, :user_id)", nativeQuery = true)
    void saveReservation(@Param("date") LocalDate date,
                         @Param("time") LocalTime time,
                         @Param("guest_count") int guestCount,
                         @Param("first_name") String firstName,
                         @Param("last_name") String lastName,
                         @Param("email") String email,
                         @Param("phone_number") String phoneNumber,
                         @Param("special_request") String specialRequest,
                         @Param("status") String status,
                         @Param("saved_at") LocalDateTime savedAt,
                         @Param("table_id") int tableId,
                         @Param("user_id") int userId);
}

