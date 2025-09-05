package com.imperialgrand.backend.common.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ErrorResponse {
   private boolean success;
   private int statusCode;
   private String error;
   private String message;
   private LocalDateTime timestamp;

    public ErrorResponse(String message, String error, int statusCode) {
        this.success = false;
        this.message = message;
        this.error = error;
        this.statusCode = statusCode;
        this.timestamp = LocalDateTime.now();
    }


}
