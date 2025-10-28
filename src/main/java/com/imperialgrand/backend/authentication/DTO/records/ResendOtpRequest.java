package com.imperialgrand.backend.authentication.DTO.records;

public record ResendOtpRequest(String verifyId,
                               String email,
                               long resendCooldownMs) {
}
