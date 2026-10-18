package com.wac.autocore.dto.booking;

import com.wac.autocore.dto.mechanic.MechanicSummaryDto;
import com.wac.autocore.dto.vechicle.VehicleSummaryDto;
import com.wac.autocore.model.enums.BookingState;

import java.time.LocalDate;
import java.util.List;

public class BookingFullDto {
    private Long id;
    private VehicleSummaryDto vehicle;
    private LocalDate date;
    private String description;
    private MechanicSummaryDto mechanic;
    private BookingState STATUS;
    private List<BookingServiceItemDto> serviceItems;

    public BookingFullDto(Long id, VehicleSummaryDto vehicle, LocalDate date, String description, MechanicSummaryDto mechanic, BookingState STATUS, List<BookingServiceItemDto> serviceItems) {
        this.id = id;
        this.vehicle = vehicle;
        this.date = date;
        this.description = description;
        this.mechanic = mechanic;
        this.STATUS = STATUS;
        this.serviceItems = serviceItems;
    }

    public Long getId() {
        return id;
    }

    public VehicleSummaryDto getVehicle() {
        return vehicle;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getDescription() {
        return description;
    }

    public MechanicSummaryDto getMechanic() {
        return mechanic;
    }

    public BookingState getStatus() {
        return STATUS;
    }

    public List<BookingServiceItemDto> getServiceItems() {
        return serviceItems;
    }
}