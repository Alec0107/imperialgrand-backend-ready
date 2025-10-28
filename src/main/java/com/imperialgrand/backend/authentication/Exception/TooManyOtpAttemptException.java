package com.imperialgrand.backend.authentication.Exception;

import org.springframework.http.HttpStatus;

import java.time.Instant;

public class TooManyOtpAttemptException extends RuntimeException {

    private final HttpStatus status;
    private final long retryAt;


    public TooManyOtpAttemptException(HttpStatus status, String message, long retryAt) {
        super(message);
        this.status = status;
        this.retryAt = retryAt;
    }

    public long getRetryAt() {
        return retryAt;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
