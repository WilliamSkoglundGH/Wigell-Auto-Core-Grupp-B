package com.wac.autocore.dto.booking;

import com.wac.autocore.dto.mechanic.MechanicSummaryDto;
import com.wac.autocore.dto.vechicle.VehicleDto;
import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;

public class BookingCreateDto {
    @NotNull
    private VehicleDto vehicle;
    @FutureOrPresent
    private LocalDate date;
    @Size(max = 200)
    private String description;
    @NotNull
    private MechanicSummaryDto mechanic;

    public BookingCreateDto(VehicleDto vehicle, LocalDate date, String description, MechanicSummaryDto mechanic) {
        this.vehicle = vehicle;
        this.date = date;
        this.description = description;
        this.mechanic = mechanic;
    }

}
