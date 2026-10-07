package com.wac.autocore.model;

import java.math.BigDecimal;
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
    @OneToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "booking_id", nullable = true)
    private Booking booking;

    @Enumerated(EnumType.STRING)
    @Column(name = "work_order_type", length = 20, nullable = false)
    private WorkOrderType type = WorkOrderType.PLANNED;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "original_work_order_id", nullable = true)
    private WorkOrder originalWorkOrder;

    @OneToMany(mappedBy = "workOrder",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    private List<WorkOrderServiceItem> serviceItems = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private WorkOrderState status = WorkOrderState.CONFIRMED;
    @Column(name = "start_time", nullable = true)
    private LocalDateTime startTime;
    @Column(name = "end_time", nullable = true)
    private LocalDateTime endTime;

    protected WorkOrder() {
    }

    public WorkOrder(Booking booking) {
        this.booking = booking;
        this.type = WorkOrderType.PLANNED;
    }

    public WorkOrder(WorkOrderType type) {
        this.type = type;
    }

    public WorkOrder(WorkOrder originalWorkOrder, WorkOrderType type) {
        this.originalWorkOrder = originalWorkOrder;
        this.type = type;
    }
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
    public WorkOrderType getType() {
        return type;
    }

    public void setType(WorkOrderType type) {
        this.type = type;
    }

    public WorkOrder getOriginalWorkOrder() {
        return originalWorkOrder;
    }

    public void setOriginalWorkOrder(WorkOrder originalWorkOrder) {
        this.originalWorkOrder = originalWorkOrder;
    }
/*
    @Override
    public String toString() {
        return id +
                " - Booking ID: " + booking.getId() +
                //" - ServiceItems: " + serviceItems.toString() +
                " - Start time: " + startTime +
                " - End time: " + endTime +
                " | Status: " + status;
    }

 */
}