package com.wac.autocore.exception;

public class WorkOrderWrongStatusException extends RuntimeException {
    public WorkOrderWrongStatusException(String message) {
        super(message);
    }
}
