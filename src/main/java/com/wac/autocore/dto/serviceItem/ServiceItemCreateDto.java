package com.wac.autocore.dto.serviceItem;


import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

public class ServiceItemCreateDto {
    @NotBlank
    @Size(min = 1, max = 60)
    private String name;
    @Size(max = 200)
    private String description;
    @NotNull
    @PositiveOrZero
    private BigDecimal price;
    @NotNull
    private int estimatedMinutes;

    protected ServiceItemCreateDto() {}

    public ServiceItemCreateDto(String name, String description, BigDecimal price, int estimatedMinutes) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.estimatedMinutes = estimatedMinutes;
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

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public int getEstimatedMinutes() {
        return estimatedMinutes;
    }
}
