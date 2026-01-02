package com.imperialgrand.backend.reservation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GuestInfoDTO {
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;

    public String getFullName(){
        return firstName + lastName;
    }

}

