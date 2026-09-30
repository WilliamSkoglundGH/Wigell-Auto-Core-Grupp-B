package com.wac.autocore.dto;

import java.time.LocalDateTime;
import java.util.List;

public class WorkOrderResponseDto {

    private final Long id;
    private final Long bookingId;
    private final String status;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private final List<WorkOrderServiceItemDto> serviceItems;

    public WorkOrderResponseDto(Long id, Long bookingId, String status, LocalDateTime startTime, LocalDateTime endTime, List<WorkOrderServiceItemDto> serviceItems) {
        this.id = id;
        this.bookingId = bookingId;
        this.status = status;
        this.startTime = startTime;
        this.endTime = endTime;
        this.serviceItems = serviceItems;
    }

    public Long getId() { return id; }
    public Long getBookingId() { return bookingId; }
    public String getStatus() { return status; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public List<WorkOrderServiceItemDto> getServiceItems() { return serviceItems; }
}