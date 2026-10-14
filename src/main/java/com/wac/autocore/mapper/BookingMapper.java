package com.wac.autocore.mapper;

import com.wac.autocore.dto.booking.BookingCloneDto;
import com.wac.autocore.dto.booking.BookingServiceItemDto;
import com.wac.autocore.dto.booking.BookingSummaryDto;
import com.wac.autocore.dto.vechicle.VehicleDto;
import com.wac.autocore.model.Booking;
import com.wac.autocore.model.BookingServiceItem;
import com.wac.autocore.model.Vehicle;

import java.util.ArrayList;
import java.util.Collections;
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
    public static BookingSummaryDto toBookingSummaryDto(Booking booking) {
        if (booking == null) {
            return null;
        }

        String vehicleBrandAndModel = "";
        String customerName = "";

        if (booking.getVehicle() != null) {
            Vehicle v = booking.getVehicle();
            String brand = v.getBrand() != null ? v.getBrand() : "";
            String model = v.getModel() != null ? v.getModel() : "";
            vehicleBrandAndModel = (brand + " " + model).trim();

            if (v.getCustomer() != null && v.getCustomer().getName() != null) {
                customerName = v.getCustomer().getName();
            }
        }

        String statusStr = booking.getStatus() != null ? booking.getStatus().name() : null;

        return new BookingSummaryDto(
                booking.getId(),
                vehicleBrandAndModel,
                customerName,
                booking.getDate(),
                booking.getDescription(),
                statusStr,
                vehi
        );
    }

    public static List<BookingSummaryDto> toBookingSummaryDtoList(List<Booking> bookings) {
        if (bookings == null) {
            return Collections.emptyList();
        }
        List<BookingSummaryDto> dtoList = new ArrayList<>();
        for (Booking booking : bookings) {
            dtoList.add(toBookingSummaryDto(booking));
        }
        return dtoList;
    }

}
