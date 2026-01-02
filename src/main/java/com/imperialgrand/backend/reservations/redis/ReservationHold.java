package com.imperialgrand.backend.reservations.redis;

import java.time.LocalDateTime;

public record ReservationHold(
        Long tableId,
        LocalDateTime Start,
        LocalDateTime end,
        int partySize,
        Long userId) {
}
