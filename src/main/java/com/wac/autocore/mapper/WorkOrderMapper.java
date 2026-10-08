package com.wac.autocore.mapper;

import com.wac.autocore.dto.workorder.WorkOrderForMechanicDto;
import com.wac.autocore.dto.workorder.WorkOrderResponseDto;
import com.wac.autocore.dto.workorder.WorkOrderServiceItemDto;
import com.wac.autocore.dto.workorder.WorkOrderSummaryDto;
import com.wac.autocore.model.*;

import java.util.List;
import java.util.stream.Collectors;

public final class WorkOrderMapper {

    private WorkOrderMapper() {
        // Privat konstruktör för att förhindra instansiering av util-klass
    }

    /**
     * Mappar en WorkOrder till WorkOrderSummaryDto (används i tabeller/listor utan serviceItems).
     */
    public static WorkOrderSummaryDto toSummaryDto(WorkOrder workOrder) {
        if (workOrder == null) {
            return null;
        }

        String mechanicName = "Ej tilldelad";
        if (workOrder.getMechanic() != null) {
            mechanicName = workOrder.getMechanic().getName(); // Byt till .getUsername() om det är det fältet som används
        }

        return new WorkOrderSummaryDto(
                workOrder.getId(),
                workOrder.getBooking() != null ? workOrder.getBooking().getId() : null,
                mechanicName, // 3:e argumentet
                workOrder.getStatus(), // 4:e argumentet
                workOrder.getStartTime(),
                workOrder.getEndTime()
        );
    }

    /**
     * Mappar en WorkOrder till WorkOrderResponseDto (används i detaljvyn med tillhörande serviceItems).
     */
    public static WorkOrderResponseDto toResponseDto(WorkOrder workOrder, List<WorkOrderServiceItemDto> serviceItems) {
        if (workOrder == null) {
            return null;
        }

        return new WorkOrderResponseDto(
                workOrder.getId(),
                workOrder.getBooking() != null ? workOrder.getBooking().getId() : null,
                workOrder.getStatus(),
                workOrder.getStartTime(),
                workOrder.getEndTime(),
                serviceItems
        );
    }

    /**
     * Mappar en enskild WorkOrderServiceItem till WorkOrderServiceItemDto.
     */
    public static WorkOrderServiceItemDto toServiceItemDto(WorkOrderServiceItem item) {
        if (item == null) {
            return null;
        }

        String serviceName = (item.getServiceItem() != null) ? item.getServiceItem().getName() : "Okänd tjänst";
        String description = (item.getServiceItem() != null) ? item.getServiceItem().getDescription() : "";

        return new WorkOrderServiceItemDto(
                serviceName,
                description,
                item.getPriceAtTime(),
                item.getDurationAtTime()
        );
    }

    /**
     * Mappar en hel lista med WorkOrderServiceItems till en DTO-lista.
     */
    public static List<WorkOrderServiceItemDto> toServiceItemDtoList(List<WorkOrderServiceItem> items) {
        if (items == null) {
            return null;
        }

        return items.stream()
                .map(WorkOrderMapper::toServiceItemDto)
                .collect(Collectors.toList());
    }

    /**
     * Mappar en lista med BookingServiceItems till en DTO-lista (används när WorkOrder har status CREATED).
     */
    public static List<WorkOrderServiceItemDto> toServiceItemDtoListFromBooking(List<BookingServiceItem> bookingItems) {
        if (bookingItems == null) {
            return null;
        }

        return bookingItems.stream().map(item -> {
            String serviceName = (item.getServiceItem() != null) ? item.getServiceItem().getName() : "Okänd tjänst";
            String description = (item.getServiceItem() != null) ? item.getServiceItem().getDescription() : "";

            return new WorkOrderServiceItemDto(
                    serviceName,
                    description,
                    item.getPriceAtTime(),
                    item.getDurationAtTime()
            );
        }).collect(Collectors.toList());
    }
    public static WorkOrderForMechanicDto toMechanicDto(WorkOrder workOrder) {
        if (workOrder == null) {
            return null;
        }

        Long bookingId = (workOrder.getBooking() != null) ? workOrder.getBooking().getId() : null;

        String vehicleReg = "-";
        if (workOrder.getVehicle() != null) {
            vehicleReg = workOrder.getVehicle().getRegistrationNumber();
        }

        String description = "-";
        if (workOrder.getDescription() != null) {
            description = workOrder.getDescription();
        }

        return new WorkOrderForMechanicDto(
                workOrder.getId(),
                bookingId,
                vehicleReg,
                workOrder.getStartTime(),
                workOrder.getEndTime(),
                description,
                workOrder.getStatus()
        );
    }
    public static List<WorkOrderServiceItem> toWorkOrderServicesFromBookingServices(Booking booking, WorkOrder workOrder) {

        List<BookingServiceItem> bookingServiceItems = booking.getServiceItems();

        List<WorkOrderServiceItem> workOrderItems = booking.getServiceItems().stream()
                .map(bItem -> new WorkOrderServiceItem(
                        workOrder,
                        bItem.getServiceItem(),
                        bItem.getPriceAtTime(),
                        bItem.getDurationAtTime()
                ))
                .collect(Collectors.toList());

        return workOrderItems;
    }

}
