package com.wac.autocore.factory;

import com.wac.autocore.dto.workorder.WorkOrderCreateDto;
import com.wac.autocore.model.*;
import com.wac.autocore.model.enums.WorkOrderState;
import com.wac.autocore.model.enums.WorkOrderType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DefaultWorkOrderFactory implements WorkOrderFactory{
    @Override
    public WorkOrder createPlanned(Booking booking) {
        if (booking == null) {
            throw new IllegalArgumentException("Booking is required.");
        }

        WorkOrder workOrder = new WorkOrder(booking);
        workOrder.setStatus(WorkOrderState.CONFIRMED);
        workOrder.setVehicle(booking.getVehicle());
        workOrder.setMechanic(booking.getMechanic());
        workOrder.setPlannedDate(booking.getDate());
        workOrder.setDescription(booking.getDescription());

        for (BookingServiceItem item : booking.getServiceItems()) {
            workOrder.addServiceItem(
                    item.getServiceItem(),
                    item.getPriceAtTime(),
                    item.getDurationAtTime());
        }
        return workOrder;
    }

    @Override
    public WorkOrder createDropIn(WorkOrderCreateDto dto) {
        if (dto == null || dto.getVehicle() == null) {
            throw new IllegalArgumentException("Vehicle is required.");
        }

        WorkOrder workOrder = new WorkOrder(WorkOrderType.DROP_IN);
        workOrder.setStatus(WorkOrderState.DRAFT);
        workOrder.setVehicle(dto.getVehicle());
        applyDto(workOrder, dto);

        for (ServiceItem item : dto.getServiceItems()) {
            workOrder.addServiceItem(
                    item,
                    item.getPrice(),
                    item.getEstimatedMinutes());
        }
        return workOrder;
    }

    @Override
    public WorkOrder createClaim(WorkOrder originalWorkOrder, WorkOrderCreateDto dto) {
        if (originalWorkOrder == null) {
            throw new IllegalArgumentException("Original work order is required.");
        }

        WorkOrder workOrder = new WorkOrder(originalWorkOrder, WorkOrderType.CLAIM);
        workOrder.setStatus(WorkOrderState.DRAFT);
        workOrder.setVehicle(originalWorkOrder.getVehicle());
        workOrder.setMechanic(originalWorkOrder.getMechanic());

        if (dto != null) {
            applyDto(workOrder, dto);
        }

        for (WorkOrderServiceItem item : originalWorkOrder.getServiceItems()) {
            ServiceItem serviceItem = item.getServiceItem();
            workOrder.addServiceItem(
                    serviceItem,
                    BigDecimal.ZERO,
                    serviceItem.getEstimatedMinutes());
        }
        return workOrder;
    }

    private void applyDto(WorkOrder workOrder, WorkOrderCreateDto dto) {
        if (dto.getMechanic() != null) {
            workOrder.setMechanic(dto.getMechanic());
        }
        workOrder.setPlannedDate(dto.getPlannedDate());
        workOrder.setDescription(dto.getDescription());
        workOrder.setCustomerInstructions(dto.getCustomerInstructions());
        workOrder.setComments(dto.getComments());
    }
}

