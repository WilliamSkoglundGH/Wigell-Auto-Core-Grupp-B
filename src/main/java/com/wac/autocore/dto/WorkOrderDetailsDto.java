package com.wac.autocore.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class WorkOrderDetailsDto {
    private Long id;
    private String status;
    private String mechanicName;
    private String vehicleRegistrationNumber;
    private String customerName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer estimatedDuration;
    private BigDecimal estimatedPrice;
    private List<WorkOrderServiceItemDto> serviceItems;
    private Long bookingId;

    public WorkOrderDetailsDto() {
    }

    public WorkOrderDetailsDto(Long id, String status, LocalDateTime startTime, LocalDateTime endTime) {
        this.id = id;
        this.status = status;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMechanicName() {
        return mechanicName;
    }

    public void setMechanicName(String mechanicName) {
        this.mechanicName = mechanicName;
    }

    public String getVehicleRegistrationNumber() {
        return vehicleRegistrationNumber;
    }

    public void setVehicleRegistrationNumber(String vehicleRegistrationNumber) {
        this.vehicleRegistrationNumber = vehicleRegistrationNumber;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public Integer getEstimatedDuration() {
        return estimatedDuration;
    }

    public void setEstimatedDuration(Integer estimatedDuration) {
        this.estimatedDuration = estimatedDuration;
    }

    public BigDecimal getEstimatedPrice() {
        return estimatedPrice;
    }

    public void setEstimatedPrice(BigDecimal estimatedPrice) {
        this.estimatedPrice = estimatedPrice;
    }

    public List<WorkOrderServiceItemDto> getServiceItems() {
        return serviceItems;
    }

    public void setServiceItems(List<WorkOrderServiceItemDto> serviceItems) {
        this.serviceItems = serviceItems;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }
}