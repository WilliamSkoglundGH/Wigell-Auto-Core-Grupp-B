package com.wac.autocore.dto.booking;

import com.wac.autocore.dto.vechicle.VehicleSummaryDto;

import java.util.List;

public class BookingCloneDto {
    private VehicleSummaryDto vehicleSummaryDto;
    private List<BookingServiceItemDto> serviceItems; //Ska vara färska snapshots från db.

    public BookingCloneDto(VehicleSummaryDto vehicleSummaryDto, List<BookingServiceItemDto> serviceItems) {
        this.vehicleSummaryDto = vehicleSummaryDto;
        this.serviceItems = serviceItems;
    }


    public VehicleSummaryDto getVehicle() {
        return vehicleSummaryDto;
    }

    public List<BookingServiceItemDto> getServiceItems() {
        return serviceItems;
    }
}
