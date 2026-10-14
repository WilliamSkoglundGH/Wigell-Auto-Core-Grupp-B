package com.wac.autocore.dto.booking;



public class BookingServiceItemDto {
    private Long bookingId;
    private Long serviceItemId;

    public BookingServiceItemDto(Long id, Long serviceItemId) {
        this.bookingId = id;
        this.serviceItemId = serviceItemId;
    }

    public Long getId() {
        return bookingId;
    }

    public Long getServiceItemId() {
        return serviceItemId;
    }

}
