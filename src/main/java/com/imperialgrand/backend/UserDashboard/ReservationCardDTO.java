package com.imperialgrand.backend.UserDashboard;

import com.imperialgrand.backend.reservations.Tables.ReservationStatus;
import com.imperialgrand.backend.reservations.Tables.Zone;

public record ReservationCardDTO(
        Integer reservationId,
        String date,        // "2025-11-07"
        String time,        // "18:30"
        int guestCount,
        String tableCode,   // e.g. "M1"
        Zone zone,        // e.g. "Window Seat"
        ReservationStatus status
) {}