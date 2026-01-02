package com.imperialgrand.backend.reservations.DTO;

import java.time.Instant;
import java.time.LocalDateTime;

public record HoldResponse(
        Long tableId,
        Long expiresAt,
        String reservationStart,
        String reservationEnd,
        String holdKey,
        String tableName,
        int partySize
)
{}
