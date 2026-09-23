package com.wac.autocore.model;

import java.time.LocalDate;
import javax.persistence.*;

@Entity
@Table(name = "booking")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;
    @Column(name = "booking_date", nullable = false)
    private LocalDate date;
    @Column(name = "description", length = 200, nullable = true)
    private String description;
    @Column(name = "status", length = 20, nullable = false)
    private String status;
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "mechanic_id", nullable = false)
    private Mechanic mechanic;

    protected Booking() {
    }

    public Booking(Vehicle vehicle, LocalDate date, String description, String status, Mechanic mechanic) {
        this.vehicle = vehicle;
        this.date = date;
        this.description = description;
        this.status = status;
        this.mechanic = mechanic;
    }

    public Long getId() {
        return id;
    }


    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
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

    public Mechanic getMechanic() {
        return mechanic;
    }

    public void setMechanic(Mechanic mechanic) {
        this.mechanic = mechanic;
    }

    @Override
    public String toString() {
        return id + " - Vehicle ID: " + vehicle.getId() +
                " | Date: " + date +
                " | Description: " + description +
                " | Status: " + status +
                " | Mechanic: " + mechanic.getName();
    }
}