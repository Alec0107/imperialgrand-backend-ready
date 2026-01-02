package com.imperialgrand.backend.websocket;


import com.imperialgrand.backend.reservations.Tables.Zone;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Builder

public record AdminReservationEvent(
        Integer   id,
        String customerName,
        Integer guestCount,
        String date,   // ISO yyyy-MM-dd
        String time,   // HH:mm
        String tableCode,
        Zone zone
) {}
