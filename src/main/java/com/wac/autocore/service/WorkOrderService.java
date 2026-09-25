package com.wac.autocore.service;

import com.wac.autocore.exception.MechanicNotAvailableException;
import com.wac.autocore.exception.ServiceItemNotFoundException;
import com.wac.autocore.exception.WorkOrderNotFoundException;
import com.wac.autocore.model.*;
import com.wac.autocore.repository.WorkOrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.datetime.DateFormatter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
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
    public List<WorkOrder> getAllWorkOrders() {
     return workOrderRepository.findAll();
    }


    @Transactional(readOnly = true)
    public WorkOrder getWorkOrderById(long id) {
        return workOrderRepository.findById(id).orElseThrow(() -> new WorkOrderNotFoundException(
                "Work order with ID: " + id + " not found"));

    }

     @Transactional
    public void saveWorkOrder(Long bookingId, List<ServiceItem> serviceItems) {
        Booking booking = bookingService.getBooking(bookingId);
        booking.setStatus("WORK_ORDER_CREATED");
        WorkOrder newWorkOrder = new WorkOrder(booking, serviceItems, "CREATED");

       workOrderRepository.save(newWorkOrder);
    }

    @Transactional
    public void startWorkOrder (WorkOrder workOrder) {
        if (workOrder == null) {
            throw new IllegalArgumentException("Work order cannot be null.");
        }
        if (!"CREATED".equals(workOrder.getStatus())) {
            throw new IllegalStateException("A work order must have status CREATED to be started.");
        }
        Booking booking = workOrder.getBooking();
        if (booking == null || booking.getMechanic() == null) {
            throw new IllegalStateException("Work order is missing booking or mechanic information.");
        }

        Mechanic mechanic = mechanicService.getMechanic(booking.getMechanic().getId());
        Booking savedBooking = bookingService.getBooking(booking.getId());
        if (!mechanic.isAvailable()) {
            throw new MechanicNotAvailableException("Mechanic not available, occupied on another workorder.");
        }

        workOrder.setStatus("IN_PROGRESS");
        savedBooking.setStatus("IN_PROGRESS");
        mechanic.setAvailable(false);

        workOrderRepository.save(workOrder);
    }

    @Transactional
    public void completeWorkOrder (WorkOrder workOrder) {
        if (workOrder == null) {
            throw new IllegalArgumentException("Work order cannot be null.");
        }
        if (!"IN_PROGRESS".equals(workOrder.getStatus())) {
            throw new IllegalStateException(" Check your work order status. Must be IN_PROGRESS to be completed. Start work order first.");
        }
        Booking booking = workOrder.getBooking();
        if (booking == null || booking.getMechanic() == null) {
            throw new IllegalStateException("Work order is missing booking or mechanic information.");
        }

        Mechanic mechanic = mechanicService.getMechanic(booking.getMechanic().getId());
        Booking savedBooking = bookingService.getBooking(booking.getId());

        workOrder.setStatus("COMPLETED");
        savedBooking.setStatus("COMPLETED");
        mechanic.setAvailable(true);

        workOrderRepository.save(workOrder);
    }

    public void countAndSetWorkTime(WorkOrder workOrder) {
        workOrder.setStartTime(LocalDateTime.now());
        workOrderRepository.save(workOrder);
        int time= countTotalMin(workOrder.getServiceItems());
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


    }

