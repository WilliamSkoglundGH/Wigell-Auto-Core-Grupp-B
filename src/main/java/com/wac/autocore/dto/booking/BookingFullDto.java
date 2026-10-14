package com.wac.autocore.dto.booking;

import com.wac.autocore.dto.mechanic.MechanicSummaryDto;
import com.wac.autocore.dto.vechicle.VehicleDto;

import java.time.LocalDate;
import java.util.List;

public class BookingFullDto {
    private Long id;
    private VehicleDto vehicle;
    private LocalDate date;
    private String description;
    private MechanicSummaryDto mechanic;
    private String status;
    private List<BookingServiceItemDto> serviceItems;

    public BookingFullDto(Long id, VehicleDto vehicle, LocalDate date, String description, MechanicSummaryDto mechanic, String status, List<BookingServiceItemDto> serviceItems) {
        this.id = id;
        this.vehicle = vehicle;
        this.date = date;
        this.description = description;
        this.mechanic = mechanic;
        this.status = status;
        this.serviceItems = serviceItems;
    }

    public Long getId() {
        return id;
    }

    public VehicleDto getVehicle() {
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

    public String getStatus() {
        return status;
    }

    public List<BookingServiceItemDto> getServiceItems() {
        return serviceItems;
    }
}