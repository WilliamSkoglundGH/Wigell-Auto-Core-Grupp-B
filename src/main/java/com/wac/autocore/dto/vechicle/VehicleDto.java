package com.wac.autocore.dto.vechicle;

import javax.persistence.Column;

public class VehicleDto {
    // För vyn i newBooking
    private Long id;
    private String registrationNumber;
    private String brand;
    private String model;

    public VehicleDto(Long id, String registrationNumber, String brand, String model) {
        this.id = id;
        this.registrationNumber = registrationNumber;
        this.brand = brand;
        this.model = model;
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

    @Override
    public String toString() {
        return id + " - " +
                brand + " " + model +
                " (" + registrationNumber + ")";
    }
}
