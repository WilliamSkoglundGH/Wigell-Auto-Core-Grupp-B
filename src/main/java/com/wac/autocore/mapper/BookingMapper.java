package com.wac.autocore.mapper;

import com.wac.autocore.dto.booking.BookingCloneDto;
import com.wac.autocore.dto.booking.BookingServiceItemDto;
import com.wac.autocore.dto.vechicle.VehicleDto;
import com.wac.autocore.model.Booking;
import com.wac.autocore.model.BookingServiceItem;
import com.wac.autocore.model.Vehicle;

import java.util.ArrayList;
import java.util.List;

public final class BookingMapper {

    private BookingMapper() {}

    public static BookingCloneDto toCloneDto(Booking booking) {
        VehicleDto vehicleDto = toVehicleDto(booking.getVehicle());
        List<BookingServiceItemDto> itemDto = toBSIDTO(booking.getServiceItems());
        return new BookingCloneDto(vehicleDto,itemDto);
    }
    public static List<BookingServiceItemDto> toBSIDTO(List<BookingServiceItem> list) {
        List<BookingServiceItemDto> dtoList = new ArrayList<>();
        list.forEach( i ->   dtoList.add(new BookingServiceItemDto(i.getId(), i.getServiceItem().getId()))
        );
    return dtoList;
    }

    public static VehicleDto toVehicleDto(Vehicle vehicle) {
        return new VehicleDto(vehicle.getId(), vehicle.getBrand(), vehicle.getModel(), vehicle.getRegistrationNumber());
    }

}
