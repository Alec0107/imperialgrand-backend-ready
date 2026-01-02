package com.imperialgrand.backend.Admin;


import com.imperialgrand.backend.reservations.Tables.Zone;

public record ReservationListItemDTO(
        Integer id,
        String customerName,
        Integer guestCount,
        String date,     // yyyy-MM-dd
        String time,     // HH:mm
        String tableCode,
        Zone zone,
        String status
) {}