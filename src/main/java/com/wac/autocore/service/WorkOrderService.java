package com.wac.autocore.service;

import com.wac.autocore.dto.workorder.*;
import com.wac.autocore.exception.*;
import com.wac.autocore.mapper.WorkOrderMapper;
import com.wac.autocore.model.*;
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

    public WorkOrderService(WorkOrderRepository workOrderRepository, BookingService bookingService, MechanicService mechanicService) {
        this.workOrderRepository = workOrderRepository;
        this.bookingService = bookingService;
        this.mechanicService = mechanicService;
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

        WorkOrderResponseDto dto;
        WorkOrder workOrder = workOrderRepository.findById(id).orElseThrow(() -> new WorkOrderNotFoundException(
                "Work order with ID: " + id + " not found"));
        if (workOrder.getStatus() == WorkOrderState.CONFIRMED) {
            List<BookingServiceItem> serviceItems = (workOrder.getBooking() != null) ? workOrder.getBooking().getServiceItems() : null;

            dto = WorkOrderMapper.toResponseDto(workOrder, WorkOrderMapper.toServiceItemDtoListFromBooking(serviceItems));
        } else {
            List<WorkOrderServiceItem> serviceItems = workOrder.getServiceItems();
            dto = WorkOrderMapper.toResponseDto(workOrder, WorkOrderMapper.toServiceItemDtoList(serviceItems));
        }
        return dto;
    }

    @Transactional
    public void saveWorkOrder(Long bookingId) {
        Booking booking = bookingService.getBooking(bookingId);
        booking.setStatus(booking.getStatus().getNext());
        WorkOrder newWorkOrder = new WorkOrder(booking);


        workOrderRepository.save(newWorkOrder);
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
        Booking booking = workOrder.getBooking();
        if (booking == null || booking.getMechanic() == null) {
            throw new IllegalStateException("Work order is missing booking or mechanic information.");
        }

        Mechanic mechanic = mechanicService.getMechanic(booking.getMechanic().getId());
        if (!mechanic.isAvailable()) {
            throw new MechanicNotAvailableException("Mechanic not available, occupied on another workorder.");
        }


        if (booking.getServiceItems() == null || booking.getServiceItems().isEmpty()) {
            throw new BookingWithoutServicesException("Booking has no Services, please add services to booking first.");
        }

        List<WorkOrderServiceItem> workOrderItems = WorkOrderMapper.toWorkOrderServicesFromBookingServices(booking, workOrder);

        workOrder.getServiceItems().clear();
        workOrder.getServiceItems().addAll(workOrderItems);
        BigDecimal estPrice = BigDecimal.ZERO;
        Integer estTime = 0;
        for (WorkOrderServiceItem item : workOrderItems) {
            if (item.getDurationAtTime() != null) {
                estTime += item.getDurationAtTime();
            }
            if (item.getPriceAtTime() != null) {
                estPrice = estPrice.add(item.getPriceAtTime()); // Spara det nya BigDecimal-värdet!
            }
        }
        workOrder.setStartTime(LocalDateTime.now());
        workOrder.setStatus(workOrder.getStatus().getNext());
        booking.setStatus(booking.getStatus().getNext());
        mechanic.setAvailable(false);

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
        Booking booking = workOrder.getBooking();
        if (booking == null || booking.getMechanic() == null) {
            throw new IllegalStateException("Work order is missing booking or mechanic information.");
        }

        Mechanic mechanic = mechanicService.getMechanic(booking.getMechanic().getId());
        workOrder.setEndTime(LocalDateTime.now());
        workOrder.setStatus(workOrder.getStatus().getNext());
        booking.setStatus(booking.getStatus().getNext());
        mechanic.setAvailable(true);

        workOrderRepository.save(workOrder);
    }

    @Transactional
    public WorkOrderDetailsDto getWorkOrderInfoById(Long workOrderId) {
//kolla status, om den är created, hämta från booking
        WorkOrder workOrder = workOrderRepository.findById(workOrderId)
                .orElseThrow(() -> new WorkOrderNotFoundException("Work order not found with ID: " + workOrderId));
        WorkOrderDetailsDto dto = new WorkOrderDetailsDto(workOrderId, workOrder.getStatus(), workOrder.getStartTime(), workOrder.getEndTime());
        Booking booking = workOrder.getBooking();
        dto.setMechanicName(booking.getMechanic().getName());
        dto.setVehicleRegistrationNumber(booking.getVehicle().getRegistrationNumber());
        dto.setCustomerName(booking.getVehicle().getCustomer().getName());

        List<WorkOrderServiceItemDto> serviceItemList;
        if (WorkOrderState.CONFIRMED.equals(dto.getStatus())) {
            serviceItemList = WorkOrderMapper.toServiceItemDtoListFromBooking(workOrder.getBooking().getServiceItems());

        } else {
            serviceItemList = WorkOrderMapper.toServiceItemDtoList(workOrder.getServiceItems());

        }
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
        dto.setBookingId(workOrder.getBooking().getId());


        return dto;
    }

    //Metod för att avbryta en workorder(tillåts initalt nu bara för en workorder med status draft eller confirmed, kan ändras om ni vill)
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

}

