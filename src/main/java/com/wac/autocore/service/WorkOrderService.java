package com.wac.autocore.service;

import com.wac.autocore.dto.WorkOrderDetailsDto;
import com.wac.autocore.dto.WorkOrderResponseDto;
import com.wac.autocore.dto.WorkOrderServiceItemDto;
import com.wac.autocore.dto.WorkOrderSummaryDto;
import com.wac.autocore.exception.*;
import com.wac.autocore.mapper.WorkOrderMapper;
import com.wac.autocore.model.*;
import com.wac.autocore.repository.WorkOrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.datetime.DateFormatter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class WorkOrderService {

    private final Logger logger = LoggerFactory.getLogger(WorkOrderService.class);
    private WorkOrderRepository workOrderRepository;
    private BookingService bookingService;
    private ServiceItemService serviceItemService;
    private MechanicService mechanicService;

    public WorkOrderService(WorkOrderRepository workOrderRepository, BookingService bookingService, ServiceItemService serviceItemService, MechanicService mechanicService) {
        this.workOrderRepository = workOrderRepository;
        this.bookingService = bookingService;
        this.serviceItemService = serviceItemService;
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
        if ("CREATED".equals(workOrder.getStatus())) {
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
        booking.setStatus("WORK_ORDER_CREATED");
        WorkOrder newWorkOrder = new WorkOrder(booking, "CREATED");


        workOrderRepository.save(newWorkOrder);
    }

    @Transactional
    public void startWorkOrder(Long workOrderId) {

        if (workOrderId == null) {
            throw new IllegalArgumentException("Work order cannot be null.");
        }
        WorkOrder workOrder = workOrderRepository.findById(workOrderId)
                .orElseThrow(() -> new WorkOrderNotFoundException("Work order not found with ID: " + workOrderId));

        if (!"CREATED".equals(workOrder.getStatus())) {
            throw new BookingNotStateCreatedException("A work order must have status CREATED to be started.");
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
        int estTime = 0;
        for(WorkOrderServiceItem item : workOrderItems) {
            estTime += item.getDurationAtTime();
            estPrice.add(item.getPriceAtTime());
        }
        workOrder.setStartTime(LocalDateTime.now());
        workOrder.setStatus("IN_PROGRESS");
        booking.setStatus("IN_PROGRESS");
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

        if (!"IN_PROGRESS".equals(workOrder.getStatus())) {
            throw new IllegalStateException(" Check your work order status. Must be IN_PROGRESS to be completed. Start work order first.");
        }
        Booking booking = workOrder.getBooking();
        if (booking == null || booking.getMechanic() == null) {
            throw new IllegalStateException("Work order is missing booking or mechanic information.");
        }

        Mechanic mechanic = mechanicService.getMechanic(booking.getMechanic().getId());
        workOrder.setEndTime(LocalDateTime.now());
        workOrder.setStatus("COMPLETED");
        booking.setStatus("COMPLETED");
        mechanic.setAvailable(true);

        workOrderRepository.save(workOrder);
    }
@Transactional
    public WorkOrderDetailsDto getWorkOrderInfoById(Long workOrderId) {

        WorkOrder workOrder = workOrderRepository.findById(workOrderId)
                .orElseThrow(() -> new WorkOrderNotFoundException("Work order not found with ID: " + workOrderId));
        WorkOrderDetailsDto dto = new WorkOrderDetailsDto(workOrderId, workOrder.getStatus(), workOrder.getStartTime(), workOrder.getEndTime());
        Booking booking = workOrder.getBooking();
        dto.setMechanicName(booking.getMechanic().getName());
        dto.setVehicleRegistrationNumber(booking.getVehicle().getRegistrationNumber());
        dto.setCustomerName(booking.getVehicle().getCustomer().getName());
        List<WorkOrderServiceItemDto> serviceItemList = WorkOrderMapper.toServiceItemDtoList(workOrder.getServiceItems());
        int estTime = 0;
        BigDecimal estPrice = BigDecimal.ZERO;
        for (WorkOrderServiceItemDto item : serviceItemList) {
            estTime += item.getDurationAtTime();
            estPrice.add(item.getPriceAtTime());
        }
        dto.setServiceItems(serviceItemList);
        return dto;
    }
/*
    public void countAndSetWorkTime(WorkOrder workOrder) {
        workOrder.setStartTime(LocalDateTime.now());
        workOrderRepository.save(workOrder);
        //TODO glöm inte byta till något vettigt !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        int time= 120;//countTotalMin(workOrder.getServiceItems());
        workOrder.setEndTime(workOrder.getStartTime().plusMinutes(time));
        workOrderRepository.save(workOrder);
    }

    public int countTotalMin(List<ServiceItem> serviceItems) {
        int totalMin = 0;
        if (serviceItems != null) {
            for (ServiceItem s : serviceItems) {
                totalMin += s.getEstimatedMinutes();
            }
        }
        return totalMin;
    }

*/
}

