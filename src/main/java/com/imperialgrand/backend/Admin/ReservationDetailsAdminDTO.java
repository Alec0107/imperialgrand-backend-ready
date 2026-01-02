package com.imperialgrand.backend.Admin;

import java.util.List;

// For the details drawer
public record ReservationDetailsAdminDTO(
        Integer id,
        String status,
        String date,      // yyyy-MM-dd
        String time,      // HH:mm
        Integer guestCount,
        String tableCode,
        String zone,
        String customerName,
        String customerEmail,
        String phoneNumber,
        String note,
        List<String> dietaryItems,
        List<String> occasionItems
) {}