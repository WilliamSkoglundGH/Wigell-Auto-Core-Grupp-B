package com.wac.autocore.service;

import com.wac.autocore.dto.workorder.*;
import com.wac.autocore.exception.*;
import com.wac.autocore.factory.WorkOrderFactory;
import com.wac.autocore.mapper.WorkOrderMapper;
import com.wac.autocore.model.*;
import com.wac.autocore.model.enums.BookingState;
import com.wac.autocore.model.enums.WorkOrderState;
import com.wac.autocore.repository.WorkOrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class WorkOrderService {

    private final Logger logger = LoggerFactory.getLogger(WorkOrderService.class);
    private WorkOrderRepository workOrderRepository;
    private BookingService bookingService;
    private MechanicService mechanicService;
    private WorkOrderFactory workOrderFactory;

    public WorkOrderService(WorkOrderRepository workOrderRepository, BookingService bookingService, MechanicService mechanicService,
                            WorkOrderFactory workOrderFactory) {
        this.workOrderRepository = workOrderRepository;
        this.bookingService = bookingService;
        this.mechanicService = mechanicService;
        this.workOrderFactory = workOrderFactory;
    }
    @Transactional(readOnly = true)
    public List<WorkOrderSummaryDto> getAllWorkOrders() {
        List<WorkOrder> workOrders = workOrderRepository.findAll();

        return workOrders.stream()
                .map(WorkOrderMapper::toSummaryDto)
                .collect(Collectors.toList());
    }
    /*
     * WorkOrder gets serviceItems from Booking if workorder has status=CREATED else it gets it from it's own data
     */
    @Transactional(readOnly = true)
    public WorkOrderResponseDto getWorkOrderById(long id) {

        WorkOrder workOrder = workOrderRepository.findById(id).orElseThrow(() -> new WorkOrderNotFoundException(
                "Work order with ID: " + id + " not found"));

            return WorkOrderMapper.toResponseDto(workOrder,
                    WorkOrderMapper.toServiceItemDtoList(workOrder.getServiceItems()));

            /*
        if (workOrder.getStatus() == WorkOrderState.CONFIRMED) {
            List<BookingServiceItem> serviceItems = (workOrder.getBooking() != null) ? workOrder.getBooking().getServiceItems() : null;

            dto = WorkOrderMapper.toResponseDto(workOrder, WorkOrderMapper.toServiceItemDtoListFromBooking(serviceItems));
        } else {
            List<WorkOrderServiceItem> serviceItems = workOrder.getServiceItems();
            dto = WorkOrderMapper.toResponseDto(workOrder, WorkOrderMapper.toServiceItemDtoList(serviceItems));
        }
        */
    }

    @Transactional
    public WorkOrder createPlannedWorkOrder(Long bookingId) {
        Booking booking = bookingService.getBooking(bookingId);
        booking.setStatus(booking.getStatus().getNext());
        WorkOrder newWorkOrder = workOrderFactory.createPlanned(booking);

        return workOrderRepository.save(newWorkOrder);
    }

    @Transactional
    public WorkOrder createDropInWorkOrder(WorkOrderCreateDto dto){
        WorkOrder workOrder = workOrderFactory.createDropIn(dto);
        return workOrderRepository.save(workOrder);
    }

    @Transactional
    public WorkOrder createClaimWorkOrder(Long originalWorkOrderId, WorkOrderCreateDto dto){
        WorkOrder workOrderOriginal = workOrderRepository.findById(originalWorkOrderId).orElseThrow(
                () -> new WorkOrderNotFoundException("Work order not found with ID: " + originalWorkOrderId));

        if(workOrderOriginal.getStatus() != WorkOrderState.COMPLETED){
            throw new WorkOrderWrongStatusException("Only completed work orders can be claimed");
        }

        WorkOrder workOrderClaim = workOrderFactory.createClaim(workOrderOriginal, dto);
        return workOrderRepository.save(workOrderClaim);
    }


    @Transactional
    public void startWorkOrder(Long workOrderId) {

        if (workOrderId == null) {
            throw new IllegalArgumentException("Work order cannot be null.");
        }
        WorkOrder workOrder = workOrderRepository.findById(workOrderId)
                .orElseThrow(() -> new WorkOrderNotFoundException("Work order not found with ID: " + workOrderId));

        if (!workOrder.getStatus().canStart()) {
            throw new WorkOrderWrongStatusException("Wrong status for start: " + workOrder.getStatus());
        }

        Mechanic mechanic = mechanicService.getMechanic(workOrder.getMechanic().getId());
        if (!mechanic.isAvailable()) {
            throw new MechanicNotAvailableException("Mechanic not available, occupied on another workorder.");
        }

        if (workOrder.getServiceItems().isEmpty()) {
            throw new BookingWithoutServicesException("Work order has no services.");
        }

        workOrder.setStartTime(LocalDateTime.now());
        workOrder.setStatus(workOrder.getStatus().getNext());
        mechanic.setAvailable(false);

        Booking booking = workOrder.getBooking();
        if (booking != null) {
            booking.setStatus(booking.getStatus().getNext());
        }

        workOrderRepository.save(workOrder);
    }

    @Transactional
    public void completeWorkOrder(Long workOrderId) {

        if (workOrderId == null) {
            throw new IllegalArgumentException("Work order cannot be null.");
        }
        WorkOrder workOrder = workOrderRepository.findById(workOrderId)
                .orElseThrow(() -> new WorkOrderNotFoundException("Work order not found with ID: " + workOrderId));

        if (!workOrder.getStatus().canComplete()) {
            throw new WorkOrderWrongStatusException("Wrong status for complete: " + workOrder.getStatus());
        }

        Mechanic mechanic = mechanicService.getMechanic(workOrder.getMechanic().getId());
        workOrder.setEndTime(LocalDateTime.now());
        workOrder.setStatus(workOrder.getStatus().getNext());
        mechanic.setAvailable(true);

        Booking booking = workOrder.getBooking();
        if (booking != null) {
            booking.setStatus(booking.getStatus().getNext());
        }

        workOrderRepository.save(workOrder);
    }

    @Transactional
    public WorkOrderDetailsDto getWorkOrderInfoById(Long workOrderId) {
//kolla status, om den är created, hämta från booking
        WorkOrder workOrder = workOrderRepository.findById(workOrderId)
                .orElseThrow(() -> new WorkOrderNotFoundException("Work order not found with ID: " + workOrderId));

        WorkOrderDetailsDto dto = new WorkOrderDetailsDto(workOrderId, workOrder.getStatus(), workOrder.getStartTime(), workOrder.getEndTime());

        dto.setMechanicName(workOrder.getMechanic() != null ? workOrder.getMechanic().getName() : "-");
        dto.setMechanicId(workOrder.getMechanic() != null ? workOrder.getMechanic().getId() : null);
        dto.setPlannedDate(workOrder.getPlannedDate());
        dto.setDescription(workOrder.getDescription());
        dto.setVehicleRegistrationNumber(workOrder.getVehicle() != null ? workOrder.getVehicle().getRegistrationNumber() : "-");
        dto.setCustomerName(workOrder.getVehicle() != null ? workOrder.getVehicle().getCustomer().getName() : "-");
        dto.setBookingId(workOrder.getBooking() != null ? workOrder.getBooking().getId() : null);

        List<WorkOrderServiceItemDto> serviceItemList = WorkOrderMapper.toServiceItemDtoList(workOrder.getServiceItems());

        Integer estTime = 0;
        BigDecimal estPrice = BigDecimal.ZERO;
        for (WorkOrderServiceItemDto item : serviceItemList) {
            if (item.getDurationAtTime() != null) {
                estTime += item.getDurationAtTime();
            }
            if (item.getPriceAtTime() != null) {
                estPrice = estPrice.add(item.getPriceAtTime());
            }
        }
        dto.setServiceItems(serviceItemList);
        dto.setEstimatedDuration(estTime);
        dto.setEstimatedPrice(estPrice);

        return dto;
    }

    @Transactional
    public void confirmWorkOrder(Long workOrderId) {
        WorkOrder workOrder = workOrderRepository.findById(workOrderId)
                .orElseThrow(() -> new WorkOrderNotFoundException("Work order not found with ID: " + workOrderId));

        if (!workOrder.getStatus().canConfirm()) {
            throw new WorkOrderWrongStatusException("Cannot confirm work order in state: " + workOrder.getStatus());
        }

        if (!workOrder.isReadyToConfirm()) {
            throw new IllegalStateException("Work order is missing vehicle, mechanic or services.");
        }

        workOrder.setStatus(workOrder.getStatus().getNext());
        workOrderRepository.save(workOrder);
    }

    @Transactional
    public void cancelWorkOrder(Long workOrderId) {
        WorkOrder workOrder = workOrderRepository.findById(workOrderId)
                .orElseThrow(() -> new WorkOrderNotFoundException(
                        "Work order not found with ID: " + workOrderId
                ));

        if (!workOrder.getStatus().canCancel()) {
            throw new WorkOrderWrongStatusException(
                    "Cannot cancel work order in state: " + workOrder.getStatus()
            );
        }

        workOrder.setStatus(WorkOrderState.CANCELED);
        workOrderRepository.save(workOrder);
    }

    @Transactional
    public void addServiceItemToWorkOrder(Long workOrderId, ServiceItem serviceItem) {
        if (serviceItem == null) {
            throw new IllegalArgumentException("Service item is required.");
        }

        WorkOrder workOrder = workOrderRepository.findById(workOrderId)
                .orElseThrow(() -> new WorkOrderNotFoundException("Work order not found with ID: " + workOrderId));

        if (!workOrder.getStatus().canEdit()) {
            throw new WorkOrderWrongStatusException("Cannot edit services in state: " + workOrder.getStatus());
        }

        if (workOrder.getBooking() != null) {
            throw new IllegalStateException("Services on a planned work order are managed through its booking.");
        }

        workOrder.addServiceItem(serviceItem, serviceItem.getPrice(), serviceItem.getEstimatedMinutes());
        workOrderRepository.save(workOrder);
    }

    @Transactional
    public void removeServiceItemFromWorkOrder(Long workOrderId, Long workOrderServiceItemId) {
        WorkOrder workOrder = workOrderRepository.findById(workOrderId)
                .orElseThrow(() -> new WorkOrderNotFoundException("Work order not found with ID: " + workOrderId));

        if (!workOrder.getStatus().canEdit()) {
            throw new WorkOrderWrongStatusException("Cannot edit services in state: " + workOrder.getStatus());
        }

        if (workOrder.getBooking() != null) {
            throw new IllegalStateException("Services on a planned work order are managed through its booking.");
        }

        WorkOrderServiceItem itemToRemove = workOrder.getServiceItems().stream()
                .filter(item -> item.getId().equals(workOrderServiceItemId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Service item not found on work order."));

        workOrder.getServiceItems().remove(itemToRemove);
        workOrderRepository.save(workOrder);
    }

    @Transactional
    public void updateWorkOrder(Long workOrderId, WorkOrderCreateDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Update data is required.");
        }

        WorkOrder workOrder = workOrderRepository.findById(workOrderId)
                .orElseThrow(() -> new WorkOrderNotFoundException("Work order not found with ID: " + workOrderId));

        if (!workOrder.getStatus().canEdit()) {
            throw new WorkOrderWrongStatusException("Cannot edit work order in state: " + workOrder.getStatus());
        }

        if (workOrder.getBooking() != null) {
            throw new IllegalStateException("A planned work order is managed through its booking.");
        }

        if (dto.getMechanic() != null) {
            workOrder.setMechanic(dto.getMechanic());
        }
        if (dto.getPlannedDate() != null) {
            workOrder.setPlannedDate(dto.getPlannedDate());
        }
        if (dto.getDescription() != null) {
            workOrder.setDescription(dto.getDescription());
        }
        if (dto.getCustomerInstructions() != null) {
            workOrder.setCustomerInstructions(dto.getCustomerInstructions());
        }
        if (dto.getComments() != null) {
            workOrder.setComments(dto.getComments());
        }

        workOrderRepository.save(workOrder);
    }


}

