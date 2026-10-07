package com.wac.autocore.dto.booking;

import java.math.BigDecimal;

public class BookingServiceItemDto {
    private final String serviceName;
    private final String description;

    public BookingServiceItemDto(String serviceName, String description) {
        this.serviceName = serviceName;
        this.description = description;
    }

    // Getters för JavaFX
    public String getServiceName() {
        return serviceName;
    }

    public String getDescription() {
        return description;
    }

}

