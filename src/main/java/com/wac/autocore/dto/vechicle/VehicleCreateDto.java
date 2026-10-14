package com.wac.autocore.dto.vechicle;

public class VehicleCreateDto {

    private String registrationNumber;
    private String brand;
    private String model;
    private int year;
    private Long customerId;

    // Tom konstruktor för ramverk
    public VehicleCreateDto() {
    }

    // Fullständig konstruktor
    public VehicleCreateDto(String registrationNumber, String brand, String model, int year, Long customerId) {
        this.registrationNumber = registrationNumber;
        this.brand = brand;
        this.model = model;
        this.year = year;
        this.customerId = customerId;
    }

    // Getters och Setters
    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }
}