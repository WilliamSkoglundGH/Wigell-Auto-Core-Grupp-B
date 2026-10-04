package com.wac.autocore.dto.workorder;

import com.wac.autocore.model.WorkOrderState;

import java.time.LocalDateTime;

public class WorkOrderSummaryDto {
    private final Long id;
    private final Long bookingId;
    private final WorkOrderState status;
    private final String mechanicName;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;

    public WorkOrderSummaryDto(Long id, Long bookingId, String mechanicName, WorkOrderState status, LocalDateTime startTime, LocalDateTime endTime) {
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

    public WorkOrderState getStatus() {
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