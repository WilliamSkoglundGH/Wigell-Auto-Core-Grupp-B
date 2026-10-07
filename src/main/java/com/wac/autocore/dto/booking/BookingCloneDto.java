package com.wac.autocore.dto.booking;

import com.wac.autocore.dto.vechicle.VehicleDto;
import com.wac.autocore.model.BookingServiceItem;
import java.util.List;

public class BookingCloneDto {
    private VehicleDto vehicleDto;
    private List<BookingServiceItem> serviceItems; //Ska vara färska snapshots från db.

    public BookingCloneDto(VehicleDto vehicleDto, List<BookingServiceItem> serviceItems) {
        this.vehicleDto = vehicleDto;
        this.serviceItems = serviceItems;
    }


    public VehicleDto getVehicle() {
        return vehicleDto;
    }

    public List<BookingServiceItem> getServiceItems() {
        return serviceItems;
    }
}
