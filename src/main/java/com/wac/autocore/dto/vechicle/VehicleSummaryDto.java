package com.wac.autocore.dto.vechicle;

public class VehicleSummaryDto {
    // För vyn i newBooking
    private Long id;
    private String registrationNumber;
    private String brand;
    private String model;
    private int year;
    private String customerName;

    public VehicleSummaryDto(Long id, String registrationNumber, String brand, String model, int year, String customerName) {
        this.id = id;
        this.registrationNumber = registrationNumber;
        this.brand = brand;
        this.model = model;
        this.year = year;
        this.customerName = customerName;
    }

    public Long getId() {
        return id;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public String getBrand() {
        return brand;
    }

    public String getModel() {
        return model;
    }

    public String getCustomerName() {
        return customerName;
    }

    public int getYear() {
        return year;
    }
    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    @Override
    public String toString() {
        return id + " - " +
                brand + " " + model +
                " (" + registrationNumber + ")";
    }
}
