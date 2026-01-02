package com.imperialgrand.backend.authentication.DTO.records;

import com.imperialgrand.backend.user.model.Role;

public record MeResponse(Long id,
                         String email,
                         String name,
                         Role role,
                         boolean isVerified) {
}
