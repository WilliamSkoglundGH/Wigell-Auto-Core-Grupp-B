package com.wac.autocore.dto.workorder;

import java.math.BigDecimal;

public class WorkOrderServiceItemDto {
    private final String serviceName;
    private final String description;
    private final BigDecimal priceAtTime;
    private final Integer durationAtTime;

    public WorkOrderServiceItemDto(String serviceName, String description, BigDecimal priceAtTime, Integer durationAtTime) {
        this.serviceName = serviceName;
        this.description = description;
        this.priceAtTime = priceAtTime;
        this.durationAtTime = durationAtTime;
    }

    // Getters för JavaFX
    public String getServiceName() {
        return serviceName;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getPriceAtTime() {
        return priceAtTime;
    }

    public Integer getDurationAtTime() {
        return durationAtTime;
    }
}