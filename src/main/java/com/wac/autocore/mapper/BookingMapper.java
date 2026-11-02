package com.wac.autocore.mapper;

import com.wac.autocore.dto.booking.BookingCloneDto;
import com.wac.autocore.dto.booking.BookingFullDto;
import com.wac.autocore.dto.booking.BookingServiceItemDto;
import com.wac.autocore.dto.booking.BookingSummaryDto;
import com.wac.autocore.dto.mechanic.MechanicSummaryDto;
import com.wac.autocore.dto.vechicle.VehicleSummaryDto;
import com.wac.autocore.model.Booking;
import com.wac.autocore.model.BookingServiceItem;
import com.wac.autocore.model.Vehicle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class BookingMapper {

    private BookingMapper() {}

    public static BookingCloneDto toCloneDto(BookingFullDto dto) {
        return new BookingCloneDto(dto.getVehicle(),dto.getServiceItems());
    }

    public static List<BookingServiceItemDto> toBServiceItemDto(List<BookingServiceItem> list) {
        List<BookingServiceItemDto> dtoList = new ArrayList<>();
        list.forEach( i ->
                dtoList.add(new BookingServiceItemDto(
                        i.getId(),
                        i.getServiceItem().getId(),
                        i.getServiceItem().getName(),
                        i.getDurationAtTime(),
                        i.getPriceAtTime())
        ));
    return dtoList;
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
                statusStr
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

    public static BookingFullDto toBookingFullDto(Booking booking) {
        if (booking == null) {
            return null;
        }
        MechanicSummaryDto mechanic = MechanicMapper.toSummaryDto(booking.getMechanic());
        return new BookingFullDto(
                booking.getId(),
                VehicleMapper.toDto(booking.getVehicle()),
                booking.getDate(),
                booking.getDescription(),
                mechanic,
                booking.getStatus(),
                toBServiceItemDto(booking.getServiceItems())
        );
    }
}
