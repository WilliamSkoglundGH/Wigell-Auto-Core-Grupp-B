package com.wac.autocore.model;

import java.math.BigDecimal;
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

    @OneToMany(mappedBy = "workOrder",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    private List<WorkOrderServiceItem> serviceItems = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private WorkOrderState status = WorkOrderState.CREATED;
    @Column(name = "start_time", nullable = true)
    private LocalDateTime startTime;
    @Column(name = "end_time", nullable = true)
    private LocalDateTime endTime;

    protected WorkOrder() {
    }

    public WorkOrder(Booking booking) {
        this.booking = booking;
    }

    /*
        public WorkOrder(Booking booking, WorkOrderState status) {
            this.booking = booking;
            this.status = status;
        }
    */
    public WorkOrder(Booking booking, List<WorkOrderServiceItem> serviceItems, WorkOrderState status, LocalDateTime startTime, LocalDateTime endTime) {
        this.booking = booking;
        this.serviceItems = serviceItems;
        this.status = status;
        this.startTime = startTime;
        this.endTime = endTime;
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

    public WorkOrderState getStatus() {
        return status;
    }

    public void setStatus(WorkOrderState status) {
        this.status = status;
    }

    public void addServiceItem(ServiceItem serviceItem, BigDecimal price, int duration) {
        WorkOrderServiceItem item = new WorkOrderServiceItem();
        item.setWorkOrder(this);
        item.setServiceItem(serviceItem);
        item.setPriceAtTime(price);
        item.setDurationAtTime(duration);
        this.serviceItems.add(item);
    }

    public void setServiceItems(List<WorkOrderServiceItem> serviceItems) {
        this.serviceItems = serviceItems;
    }

    public List<WorkOrderServiceItem> getServiceItems() {
        return serviceItems;
    }

    @Override
    public String toString() {
        return id +
                " - Booking ID: " + booking.getId() +
                //" - ServiceItems: " + serviceItems.toString() +
                " - Start time: " + startTime +
                " - End time: " + endTime +
                " | Status: " + status;
    }


}