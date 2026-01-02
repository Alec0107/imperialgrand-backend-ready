package com.imperialgrand.backend.authentication.DTO;

public record AccountDetailsDTO(
        String name,
        String email,
        String phone,
        String birthday // "YYYY-MM-DD" or null
) {}
