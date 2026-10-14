package com.wac.autocore.dto.booking;


import java.math.BigDecimal;

public class BookingServiceItemDto {
    private Long bookingId;
    private Long serviceItemId;
    private String serviceItemName;
    private int durationAtTime;
    private BigDecimal priceAtTime;

    public BookingServiceItemDto(Long bookingId, Long serviceItemId, String serviceItemName, int durationAtTime, BigDecimal priceAtTime) {
        this.bookingId = bookingId;
        this.serviceItemId = serviceItemId;
        this.serviceItemName = serviceItemName;
        this.durationAtTime = durationAtTime;
        this.priceAtTime = priceAtTime;
    }

    public int getDurationAtTime() {
        return durationAtTime;
    }

    public BigDecimal getPriceAtTime() {
        return priceAtTime;
    }

    public Long getId() {
        return bookingId;
    }

    public Long getServiceItemId() {
        return serviceItemId;
    }

    public String getName() {
        return serviceItemName;
    }
}
