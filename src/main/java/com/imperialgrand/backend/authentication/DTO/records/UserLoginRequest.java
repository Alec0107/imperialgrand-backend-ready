package com.imperialgrand.backend.authentication.DTO.records;

public record UserLoginRequest(
        String email,
        String password
){}
