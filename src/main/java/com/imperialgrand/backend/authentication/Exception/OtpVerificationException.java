package com.imperialgrand.backend.authentication.Exception;

import org.springframework.http.HttpStatus;

public class OtpVerificationException extends RuntimeException {

    private final HttpStatus status;

    public OtpVerificationException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
