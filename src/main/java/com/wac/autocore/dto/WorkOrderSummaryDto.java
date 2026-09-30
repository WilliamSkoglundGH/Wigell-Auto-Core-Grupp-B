package com.wac.autocore.dto;

import java.time.LocalDateTime;

public class WorkOrderSummaryDto {
    private final Long id;
    private final Long bookingId;
    private final String status;
    private final String mechanicName;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;

    public WorkOrderSummaryDto(Long id, Long bookingId, String mechanicName, String status, LocalDateTime startTime, LocalDateTime endTime) {
        this.id = id;
        this.bookingId = bookingId;
        this.mechanicName = mechanicName;
        this.status = status;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    // Getters för att JavaFX ska kunna läsa av fälten
    public Long getId() {
        return id;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public String getMechanicName() {
        return mechanicName;
    }
}