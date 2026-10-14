package com.wac.autocore.dto.booking;


import java.math.BigDecimal;

public class BookingServiceItemDto {
    private Long bookingId;
    private Long serviceItemId;
    private int durationAtTime;
    private BigDecimal priceAtTime;


    public BookingServiceItemDto(Long bookingId, Long serviceItemId, int durationAtTime, BigDecimal priceAtTime) {
        this.bookingId = bookingId;
        this.serviceItemId = serviceItemId;
        this.durationAtTime = durationAtTime;
        this.priceAtTime = priceAtTime;
    }


    public int getDurationAtTime() {
        return durationAtTime;
    }

    public void setDurationAtTime(int durationAtTime) {
        this.durationAtTime = durationAtTime;
    }

    public BigDecimal getPriceAtTime() {
        return priceAtTime;
    }

    public void setPriceAtTime(BigDecimal priceAtTime) {
        this.priceAtTime = priceAtTime;
    }

    public Long getId() {
        return bookingId;
    }

    public Long getServiceItemId() {
        return serviceItemId;
    }

}
