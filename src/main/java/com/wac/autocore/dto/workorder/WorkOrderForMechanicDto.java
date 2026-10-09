package com.wac.autocore.dto.workorder;

import com.wac.autocore.model.enums.WorkOrderState;
import java.time.LocalDateTime;

public class WorkOrderForMechanicDto {
    private final Long id;
    private final Long bookingId;
    private final String vehicleReg;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private final String description;
    private final WorkOrderState status;

    public WorkOrderForMechanicDto(Long id, Long bookingId, String vehicleReg, LocalDateTime startTime, LocalDateTime endTime, String description, WorkOrderState status) {
        this.id = id;
        this.bookingId = bookingId;
        this.vehicleReg = vehicleReg;
        this.startTime = startTime;
        this.endTime = endTime;
        this.description = description;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public String getVehicleReg() {
        return vehicleReg;
    }

    public String getDescription() {
        return description;
    }

    public WorkOrderState getStatus() {
        return status;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }
}