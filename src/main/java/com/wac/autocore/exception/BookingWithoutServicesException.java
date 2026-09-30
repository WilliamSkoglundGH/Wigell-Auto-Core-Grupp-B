package com.wac.autocore.exception;

public class BookingWithoutServicesException extends RuntimeException {
    public BookingWithoutServicesException(String message) {
        super(message);
    }
}
