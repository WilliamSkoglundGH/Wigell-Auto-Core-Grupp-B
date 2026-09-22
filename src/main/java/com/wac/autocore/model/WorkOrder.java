package com.wac.autocore.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
@Entity
@Table(name = "work_order")
public class WorkOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne(mappedBy = "booking")
    private Booking booking;
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "work_order_services",
            joinColumns = @JoinColumn(name = "work_order_id"),
            inverseJoinColumns = @JoinColumn(name = "service_item_id")
    )
    private List<ServiceItem> serviceItems = new ArrayList<>();
    @Column(name = "status", length = 20, nullable = true)
    private String status;
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startDateTime;
    @Column(name = "end_time", nullable = false)
    private LocalDateTime endDateTime;

    protected WorkOrder() {
    }

    public int getBookingId() {
        return bookingId;
    }

    public void setBookingId(int bookingId) {
        this.bookingId = bookingId;
    }

    public List<Integer> getServiceItemIds() {
        return serviceItemIds;
    }

    public void setServiceItemIds(List<Integer> serviceItemIds) {
        this.serviceItemIds = serviceItemIds;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void addServiceItem(int serviceItemId) {
        serviceItemIds.add(serviceItemId);
    }

    public void removeServiceItem(int serviceItemId) {
        serviceItemIds.remove(Integer.valueOf(serviceItemId));
    }

    @Override
    public String toString() {
        return id +
                " - Booking ID: " + bookingId +
                " | Mechanic ID: " + mechanicId +
                " | Services: " + serviceItemIds +
                " | Status: " + status;
    }
}