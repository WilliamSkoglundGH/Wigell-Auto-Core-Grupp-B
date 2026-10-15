package com.wac.autocore.service;

import com.wac.autocore.dto.mechanic.MechanicSummaryDto;
import com.wac.autocore.dto.serviceItem.ServiceItemSummaryDto;
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
    private VehicleService vehicleService;
    private ServiceItemService serviceItemService;
    private WorkOrderFactory workOrderFactory;

    public WorkOrderService(WorkOrderRepository workOrderRepository, BookingService bookingService, MechanicService mechanicService,
                            VehicleService vehicleService, ServiceItemService serviceItemService,WorkOrderFactory workOrderFactory) {
        this.workOrderRepository = workOrderRepository;
        this.bookingService = bookingService;
        this.mechanicService = mechanicService;
        this.vehicleService = vehicleService;
        this.serviceItemService = serviceItemService;
        this.workOrderFactory = workOrderFactory;
    }
    @Transactional(readOnly = true)
    public List<WorkOrderSummaryDto> getAllWorkOrders() {
        List<WorkOrder> workOrders = workOrderRepository.findAll();

        return workOrders.stream()
                .map(WorkOrderMapper::toSummaryDto)
                .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public WorkOrderResponseDto getWorkOrderById(long id) {

        WorkOrder workOrder = workOrderRepository.findById(id).orElseThrow(() -> new WorkOrderNotFoundException(
                "workorder.error.work_order_not_found"));

            return WorkOrderMapper.toResponseDto(workOrder,
                    WorkOrderMapper.toServiceItemDtoList(workOrder.getServiceItems()));

    }

    @Transactional
    public WorkOrderSummaryDto createPlannedWorkOrder(Long bookingId) {
        if (bookingId == null) {
            throw new BookingNotFoundException("booking.error.not_found");
        }

        Booking booking = bookingService.getBookingEntity(bookingId);

        if (booking.getStatus() != BookingState.BOOKED) {
            throw new BookingWrongStatusException(
                    "workorder.error.booking_not_booked");
        }

        booking.setStatus(booking.getStatus().getNext());

        WorkOrder newWorkOrder = workOrderFactory.createPlanned(booking);
        WorkOrder saved = workOrderRepository.save(newWorkOrder);

        return WorkOrderMapper.toSummaryDto(saved);
    }

    @Transactional
    public WorkOrderSummaryDto createDropInWorkOrder(WorkOrderCreateDto dto){
        if(dto == null){
            throw new IllegalArgumentException("Workorder data is required");
        }

        if (dto.getVehicleId() == null) {
            throw new VehicleNotFoundException("workorder.error.select_vehicle");
        }

        Vehicle vehicle = vehicleService.getVehicleEntity(dto.getVehicleId());

        Mechanic mechanic = dto.getMechanicId() != null ? mechanicService.getMechanicEntity(dto.getMechanicId()) : null;

        List<ServiceItem> serviceItems = serviceItemService.getAllServiceItemsEntity(dto.getServiceItemsIds());

        WorkOrder workOrderDropIn = workOrderFactory.createDropIn(dto, vehicle, mechanic, serviceItems);

        WorkOrder savedWorkOrder = workOrderRepository.save(workOrderDropIn);
        return WorkOrderMapper.toSummaryDto(savedWorkOrder);
    }

    @Transactional
    public WorkOrderSummaryDto createClaimWorkOrder(Long originalWorkOrderId, WorkOrderCreateDto dto){
        if (originalWorkOrderId == null) {
            throw new WorkOrderNotFoundException("workorder.error.work_order_not_found");
        }

        WorkOrder workOrderOriginal = workOrderRepository.findById(originalWorkOrderId).orElseThrow(
                () -> new WorkOrderNotFoundException("workorder.error.work_order_not_found"));

        if(workOrderOriginal.getStatus() != WorkOrderState.COMPLETED){
            throw new WorkOrderWrongStatusException("workorder.error.claim_requires_completed");
        }

        WorkOrder workOrderClaim = workOrderFactory.createClaim(workOrderOriginal, dto);
        WorkOrder saved = workOrderRepository.save(workOrderClaim);

        return WorkOrderMapper.toSummaryDto(saved);
    }


    @Transactional
    public void startWorkOrder(Long workOrderId) {
        if (workOrderId == null) {
            throw new WorkOrderNotFoundException("workorder.error.work_order_not_found");
        }
        WorkOrder workOrder = workOrderRepository.findById(workOrderId)
                .orElseThrow(() -> new WorkOrderNotFoundException("workorder.error.work_order_not_found"));

        if (!workOrder.getStatus().canStart()) {
            throw new WorkOrderWrongStatusException("workorder.error.work_order_wrong_status");
        }

        Mechanic mechanic = mechanicService.getMechanicEntity(workOrder.getMechanic().getId());
        if (!mechanic.isAvailable()) {
            throw new MechanicNotAvailableException("workorder.error.mechanic_not_available");
        }

        if (workOrder.getServiceItems().isEmpty()) {
            throw new BookingWithoutServicesException("serviceItem.error.no_service_items");
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
            throw new WorkOrderNotFoundException("workorder.error.work_order_not_found");
        }
        WorkOrder workOrder = workOrderRepository.findById(workOrderId)
                .orElseThrow(() -> new WorkOrderNotFoundException("workorder.error.work_order_not_found"));

        if (!workOrder.getStatus().canComplete()) {
            throw new WorkOrderWrongStatusException("workorder.error.work_order_wrong_status");
        }

        Mechanic mechanic = mechanicService.getMechanicEntity(workOrder.getMechanic().getId());
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
        if (workOrderId == null) {
            throw new WorkOrderNotFoundException("workorder.error.work_order_not_found");
        }

        WorkOrder workOrder = workOrderRepository.findById(workOrderId)
                .orElseThrow(() -> new WorkOrderNotFoundException("workorder.error.work_order_not_found"));

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
        if (workOrderId == null) {
            throw new WorkOrderNotFoundException("workorder.error.work_order_not_found");
        }

        WorkOrder workOrder = workOrderRepository.findById(workOrderId)
                .orElseThrow(() -> new WorkOrderNotFoundException("workorder.error.work_order_not_found"));

        if (!workOrder.getStatus().canConfirm()) {
            throw new WorkOrderWrongStatusException("workorder.error.work_order_wrong_status");
        }

        if (!workOrder.isReadyToConfirm()) {
            throw new WorkOrderWrongStatusException("workorder.error.not_ready_to_confirm");
        }

        workOrder.setStatus(workOrder.getStatus().getNext());
        workOrderRepository.save(workOrder);
    }

    @Transactional
    public void cancelWorkOrder(Long workOrderId) {
        if (workOrderId == null) {
            throw new WorkOrderNotFoundException("workorder.error.work_order_not_found");
        }

        WorkOrder workOrder = workOrderRepository.findById(workOrderId)
                .orElseThrow(() -> new WorkOrderNotFoundException("workorder.error.work_order_not_found" ));

        if (!workOrder.getStatus().canCancel()) {
            throw new WorkOrderWrongStatusException(
                    "workorder.error.work_order_wrong_status");
        }

        workOrder.setStatus(WorkOrderState.CANCELED);
        workOrderRepository.save(workOrder);
    }

    @Transactional
    public void addServiceItemToWorkOrder(Long workOrderId, Long serviceItemId) {
        if (workOrderId == null) {
            throw new WorkOrderNotFoundException("workorder.error.work_order_not_found");
        }

        if (serviceItemId == null) {
            throw new ServiceItemNotFoundException("serviceItem.error.no_service_items");
        }

        WorkOrder workOrder = workOrderRepository.findById(workOrderId)
                .orElseThrow(() -> new WorkOrderNotFoundException( "workorder.error.work_order_not_found"));

        if (!workOrder.getStatus().canEdit()) {
            throw new WorkOrderWrongStatusException("workorder.error.not_editable");
        }

        if (workOrder.getBooking() != null) {
            throw new WorkOrderWrongStatusException("workorder.error.planned_workorder");
        }

        ServiceItem serviceItem = serviceItemService.getServiceItemEntity(serviceItemId);
        workOrder.addServiceItem(serviceItem, serviceItem.getPrice(), serviceItem.getEstimatedMinutes());
        workOrderRepository.save(workOrder);
    }

    @Transactional
    public void removeServiceItemFromWorkOrder(Long workOrderId, Long workOrderServiceItemId) {
        if (workOrderId == null) {
            throw new WorkOrderNotFoundException("workorder.error.work_order_not_found");
        }

        if (workOrderServiceItemId == null) {
            throw new ServiceItemNotFoundException("serviceItem.error.no_service_items");
        }

        WorkOrder workOrder = workOrderRepository.findById(workOrderId)
                .orElseThrow(() -> new WorkOrderNotFoundException("workorder.error.work_order_not_found"));

        if (!workOrder.getStatus().canEdit()) {
            throw new WorkOrderWrongStatusException("workorder.error.not_editable");
        }

        if (workOrder.getBooking() != null) {
            throw new WorkOrderWrongStatusException("workorder.error.planned_workorder");
        }

        WorkOrderServiceItem itemToRemove = workOrder.getServiceItems().stream()
                .filter(item -> item.getId().equals(workOrderServiceItemId))
                .findFirst()
                .orElseThrow(() -> new ServiceItemNotFoundException("serviceItem.error.could_not_load"));

        workOrder.getServiceItems().remove(itemToRemove);
        workOrderRepository.save(workOrder);
    }

    @Transactional
    public void updateWorkOrder(Long workOrderId, WorkOrderCreateDto dto) {
        if (workOrderId == null) {
            throw new WorkOrderNotFoundException("workorder.error.work_order_not_found");
        }

        if (dto == null) {
            throw new IllegalArgumentException("Update data is required.");
        }

        WorkOrder workOrder = workOrderRepository.findById(workOrderId)
                .orElseThrow(() -> new WorkOrderNotFoundException("workorder.error.work_order_not_found"));

        if (!workOrder.getStatus().canEdit()) {
            throw new WorkOrderWrongStatusException("workorder.error.not_editable");
        }

        if (workOrder.getBooking() != null) {
            throw new WorkOrderWrongStatusException("workorder.error.planned_workorder");
        }

        if (dto.getMechanicId() != null) {
            Mechanic mechanic = mechanicService.getMechanicEntity(dto.getMechanicId());
            workOrder.setMechanic(mechanic);
        }
        if (dto.getPlannedDate() != null) {
            workOrder.setPlannedDate(dto.getPlannedDate());
        }
        if (dto.getDescription() != null) {
            workOrder.setDescription(dto.getDescription());
        }

        workOrderRepository.save(workOrder);
    }
}

