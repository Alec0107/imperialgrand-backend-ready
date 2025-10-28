package com.imperialgrand.backend.authentication.DTO.records;

public record VerifyRequest(
        String verifyId,
        String otp,
        String email
){}
