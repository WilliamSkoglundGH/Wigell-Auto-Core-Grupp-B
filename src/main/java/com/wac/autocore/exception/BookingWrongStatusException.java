package com.wac.autocore.exception;

public class BookingWrongStatusException extends RuntimeException {
    public BookingWrongStatusException(String message) {
        super(message);
    }
}
