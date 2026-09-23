package com.wac.autocore.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.*;
@Entity
@Table(name = "work_order")
public class WorkOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "work_order_services",
            joinColumns = @JoinColumn(name = "work_order_id"),
            inverseJoinColumns = @JoinColumn(name = "service_item_id")
    )
    private List<ServiceItem> serviceItems = new ArrayList<>();
    @Column(name = "status", length = 20, nullable = false)
    private String status;
    @Column(name = "start_time", nullable = true)
    private LocalDateTime startTime;
    @Column(name = "end_time", nullable = true)
    private LocalDateTime endTime;

    protected WorkOrder() {
    }

    public WorkOrder(Booking booking, List<ServiceItem> serviceItems, String status) {
        this.booking = booking;
        this.serviceItems = serviceItems;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public Booking getBooking() {
        return booking;
    }

    public void setBooking(Booking booking) {
        this.booking = booking;
    }

    public List<ServiceItem> getServiceItems() {
        return serviceItems;
    }

    public void setServiceItems(List<ServiceItem> serviceItems) {
        this.serviceItems = serviceItems;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

public void addServiceItem(ServiceItem serviceItem) {
        this.serviceItems.add(serviceItem);
}

    @Override
    public String toString() {
        return id +
                " - Booking ID: " + booking.getId() +
                " - ServiceItems: " + serviceItems.toString() +
                " - Start time: " + startTime +
                " - End time: " + endTime +
                " | Status: " + status;
    }
}