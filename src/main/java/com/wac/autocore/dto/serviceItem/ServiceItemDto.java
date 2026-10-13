package com.wac.autocore.dto.serviceItem;


import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

public class ServiceItemDto {
    @NotNull
    private Long id;
    @NotEmpty
    private String name;
    private String description;
    @NotNull
    private BigDecimal price;
    @NotNull
    private int estimatedMinutes;


    public ServiceItemDto(Long id, String name, String description, BigDecimal price, int estimatedMinutes) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.estimatedMinutes = estimatedMinutes;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public int getEstimatedMinutes() {
        return estimatedMinutes;
    }
}
