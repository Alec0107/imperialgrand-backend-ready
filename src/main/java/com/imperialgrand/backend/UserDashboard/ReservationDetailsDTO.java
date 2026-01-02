package com.imperialgrand.backend.UserDashboard;

import com.imperialgrand.backend.reservations.Tables.Zone;

public record ReservationDetailsDTO(
        Integer id,
        String date,                 // e.g. "2025-11-21"
        String time,                 // e.g. "18:30"
        int guests,
        String tableCode,            // r.getTable().getCode()
        Zone tableZone,            // r.getTable().getZone()
        java.util.List<String> occasions,
        java.util.List<String> dietaryRestrictions,
        String specialRequests
) {}