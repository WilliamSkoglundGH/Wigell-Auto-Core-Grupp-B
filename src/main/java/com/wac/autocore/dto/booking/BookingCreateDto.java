package com.wac.autocore.dto.booking;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public class BookingCreateDto {
    @NotNull
    private Long vehicleId;
    @FutureOrPresent
    private LocalDate date;
    @Size(max = 200)
    private String description;
    @NotNull
    private Long mechanicId;

    public BookingCreateDto(Long vehicleId, LocalDate date, String description, Long mechanicId) {
        this.vehicleId = vehicleId;
        this.date = date;
        this.description = description;
        this.mechanicId = mechanicId;
    }

    public Long getVehicleId() {
        return vehicleId;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getDescription() {
        return description;
    }

    public Long getMechanicId() {
        return mechanicId;
    }
}
