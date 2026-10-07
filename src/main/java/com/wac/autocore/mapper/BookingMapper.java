package com.wac.autocore.mapper;

import com.wac.autocore.dto.booking.BookingCloneDto;
import com.wac.autocore.dto.vechicle.VehicleDto;
import com.wac.autocore.model.Booking;
import com.wac.autocore.model.Vehicle;

public final class BookingMapper {

    private BookingMapper() {}

    public static BookingCloneDto toCloneDto(Booking booking) {
        VehicleDto vehicleDto = toVehicleDto(booking.getVehicle());
        return new BookingCloneDto(vehicleDto,booking.getServiceItems());

    }

    // I Vilket skede behöver man dto för att visa upp i newBooking? Vart tar man datan ifrån? :)

    public static VehicleDto toVehicleDto(Vehicle vehicle) {
        return new VehicleDto(vehicle.getId(), vehicle.getBrand(), vehicle.getModel(), vehicle.getRegistrationNumber());
    }

}
