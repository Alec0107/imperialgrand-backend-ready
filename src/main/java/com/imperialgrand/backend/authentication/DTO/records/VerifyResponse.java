package com.imperialgrand.backend.authentication.DTO.records;

public record VerifyResponse (
        boolean success,
        String message,
        String next_step
){}
