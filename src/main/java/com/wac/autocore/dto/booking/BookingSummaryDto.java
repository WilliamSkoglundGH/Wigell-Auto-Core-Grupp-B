package com.wac.autocore.dto.booking;

import com.wac.autocore.model.Vehicle;

import java.time.LocalDate;

public class BookingSummaryDto {

    private Long id;
    private String vehicleBrandAndModel;
    private String customerName;
    private LocalDate date;
    private String description;
    private String status;


    protected BookingSummaryDto() {}

    // Fullständig konstruktor
    public BookingSummaryDto(Long id, String vehicleBrandAndModel, String customerName, LocalDate date, String description, String status) {
        this.id = id;
        this.vehicleBrandAndModel = vehicleBrandAndModel;
        this.customerName = customerName;
        this.date = date;
        this.description = description;
        this.status = status;
    }

    // Getters och Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getVehicleBrandAndModel() {
        return vehicleBrandAndModel;
    }

    public void setVehicleBrandAndModel(String vehicleBrandAndModel) {
        this.vehicleBrandAndModel = vehicleBrandAndModel;}

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}