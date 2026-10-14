package com.wac.autocore.dto.serviceItem;


import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

public class ServiceItemDto {
    @NotNull
    private Long id;
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

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
    public int getEstimatedMinutes() {
        return estimatedMinutes;
    }
}
