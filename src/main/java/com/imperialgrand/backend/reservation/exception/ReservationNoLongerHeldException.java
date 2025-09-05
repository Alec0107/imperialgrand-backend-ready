package com.imperialgrand.backend.reservation.exception;

public class ReservationNoLongerHeldException extends RuntimeException {
    public ReservationNoLongerHeldException(String message) {
        super(message);
    }
}
