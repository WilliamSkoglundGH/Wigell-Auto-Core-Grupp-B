package com.wac.autocore.dto.booking;



public class BookingServiceItemDto {
    private Long id;
    private Long serviceItemId;

    public BookingServiceItemDto(Long id, Long serviceItemId) {
        this.id = id;
        this.serviceItemId = serviceItemId;
    }

    public Long getId() {
        return id;
    }

    public Long getServiceItemId() {
        return serviceItemId;
    }
}
