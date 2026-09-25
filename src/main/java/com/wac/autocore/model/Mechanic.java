package com.wac.autocore.model;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "mechanic")
public class Mechanic {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "name", length = 60, nullable = false)
    private String name;
    @Column(name = "phone", length = 20, nullable = false)
    private String phone;
    @Column(name = "specialization", length = 200, nullable = true)
    private String specialization;
    @Column(name = "available")
    private boolean available;

    protected Mechanic() {
    }

    public Mechanic(String name, String phone, String specialization) {
        this.name = name;
        this.phone = phone;
        this.specialization = specialization;
        this.available = true;
    }

    @OneToMany(mappedBy = "mechanic", fetch = FetchType.LAZY)
    private List<Booking> bookings;

    public List<Booking> getBookings() {
        return bookings;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    @Override
    public String toString() {
        return id + " - " + name +
                " | Phone: " + phone +
                " | Specialization: " + specialization +
                " | Available: " + (available ? "Yes" : "No");
    }
}