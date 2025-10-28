package com.imperialgrand.backend.authentication.DTO;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSignupRequest {
    private String name;
    private String email;
    private String password;
    private String phoneNumber;
}
