package com.imperialgrand.backend.authentication.DTO.records;


public record UserSignupRequest(String name,
     String email,
     String password,
     String phoneNumber
){}
