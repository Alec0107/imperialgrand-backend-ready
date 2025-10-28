package com.imperialgrand.backend.authentication.Exception;

public class TooManyOtpAttemptException extends RuntimeException {
  public TooManyOtpAttemptException(String message) {
    super(message);
  }
}
