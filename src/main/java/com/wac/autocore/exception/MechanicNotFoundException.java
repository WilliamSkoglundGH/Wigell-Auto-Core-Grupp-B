package com.wac.autocore.exception;

public class MechanicNotFoundException extends RuntimeException {

    public MechanicNotFoundException() {
        super();
    }

    public MechanicNotFoundException(String message) {
        super(message);
    }

    public MechanicNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}