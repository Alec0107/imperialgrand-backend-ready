package com.imperialgrand.backend.authentication.DTO;

public record SignUpResponse(String verifyId,
                             String email,
                             String maskedEmail,
                             int codeTTLMins,
                             int resendCooldownSecond,
                             String message) {
}
