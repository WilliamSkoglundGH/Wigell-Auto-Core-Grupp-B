package com.wac.autocore.model;
import javax.persistence.*;
@Entity
@Table(name = "vehicle")
public class Vehicle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "registration_number", length = 60, nullable = false)
    private String registrationNumber;
    @Column(name = "brand", length = 60, nullable = false)
    private String brand;
    @Column(name = "model", length = 60, nullable = false)
    private String model;
    @Column(name = "year", nullable = false)
    private int year;
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    protected Vehicle() {
    }

    public Vehicle(String registrationNumber, String brand, String model, int year, Customer customer) {
        this.registrationNumber = registrationNumber;
        this.brand = brand;
        this.model = model;
        this.year = year;
        this.customer = customer;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Long getId() {
        return id;
    }

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



    @Override
    public String toString() {
        return id + " - " +
                registrationNumber + " | " +
                brand + " " + model +
                " | Year: " + year +
                " | Customer ID: " + customer.getId();
    }
}