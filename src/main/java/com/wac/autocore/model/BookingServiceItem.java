package com.wac.autocore.model;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "booking_services")
public class BookingServiceItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_item_id", nullable = false)
    private ServiceItem serviceItem;

    @Column(name = "price_at_time", precision = 10, scale = 2, nullable = false)
    private BigDecimal priceAtTime;

    @Column(name = "duration_at_time", nullable = false)
    private int durationAtTime;

    protected BookingServiceItem() {
    }

    public BookingServiceItem(Booking booking, ServiceItem serviceItem, BigDecimal priceAtTime, int durationAtTime) {
        this.booking = booking;
        this.serviceItem = serviceItem;
        this.priceAtTime = priceAtTime;
        this.durationAtTime = durationAtTime;
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

    public ServiceItem getServiceItem() {
        return serviceItem;
    }

    public void setServiceItem(ServiceItem serviceItem) {
        this.serviceItem = serviceItem;
    }

    public BigDecimal getPriceAtTime() {
        return priceAtTime;
    }

    public void setPriceAtTime(BigDecimal priceAtTime) {
        this.priceAtTime = priceAtTime;
    }

    public int getDurationAtTime() {
        return durationAtTime;
    }

    public void setDurationAtTime(int durationAtTime) {
        this.durationAtTime = durationAtTime;
    }
}
