package com.wac.autocore.dto.workorder;

import java.math.BigDecimal;

public class WorkOrderServiceItemDto {
    private final Long id;
    private final Long serviceItemId;
    private final String serviceName;
    private final String description;
    private final BigDecimal priceAtTime;
    private final Integer durationAtTime;

    public WorkOrderServiceItemDto(String serviceName, Long serviceItemId, String description, BigDecimal priceAtTime, Integer durationAtTime,
                                   Long id) {
        this.serviceName = serviceName;
        this.serviceItemId = serviceItemId;
        this.description = description;
        this.priceAtTime = priceAtTime;
        this.durationAtTime = durationAtTime;
        this.id = id;
    }

    // Getters för JavaFX


    public Long getId() {
        return id;
    }

    public Long getServiceItemId() {
        return serviceItemId;
    }

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