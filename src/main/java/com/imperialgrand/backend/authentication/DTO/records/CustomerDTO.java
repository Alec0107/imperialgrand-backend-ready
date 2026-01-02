package com.imperialgrand.backend.authentication.DTO.records;

import com.imperialgrand.backend.authentication.DTO.User;

import java.time.Instant;
import java.time.LocalDate;

public record CustomerDTO(
        Long id,
        String name,
        String email,
        String phone,
        LocalDate birthday,
        boolean emailVerified,
        Instant createdAt
) {
    public static CustomerDTO fromEntity(User u) {
        return new CustomerDTO(
                u.getId(),
                u.getName(),
                u.getEmail(),
                u.getPhone(),
                u.getBirthday(),
                u.isEmailVerified(),
                u.getCreatedAt()
        );
    }
}