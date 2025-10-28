package com.imperialgrand.backend.authentication.DTO.records;

public record SignUpResponse(String verifyId,
                             String email,
                             String maskedEmail,
                             long verifyCooldown,
                             long resendCooldownMs,
                             String message
){}
