package com.wac.autocore.dto.booking;

import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
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

}
