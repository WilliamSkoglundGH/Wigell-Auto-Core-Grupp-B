package com.wac.autocore.service;

import com.wac.autocore.exception.ServiceItemNotFoundException;
import com.wac.autocore.exception.WorkOrderNotFoundException;
import com.wac.autocore.model.*;
import com.wac.autocore.repository.WorkOrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
@Service
public class WorkOrderService {

    private final Logger logger = LoggerFactory.getLogger(WorkOrderService.class);
     private WorkOrderRepository workOrderRepository;
     private BookingService bookingService;
     private ServiceItemService serviceItemService;

    protected WorkOrderService() {
    }

    public WorkOrderService(WorkOrderRepository workOrderRepository) {
        this.workOrderRepository = workOrderRepository;
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

    //Create
    // Skapa en ny WORKORDER - BOOKING - SERVICEITEM och STATUS - STATUS CREATED - SÄTT BOOKINGS status- WORK_ORDER CREATED
    @Transactional
    public WorkOrder saveWorkOrder(Long bookingId, List<Long> serviceItemId, String status) {
        //Lägg till serviceItems i listan på Workorder.;
        Booking booking = bookingService.getBooking(bookingId);

        WorkOrder newWorkOrder = new WorkOrder(booking, serviceItems, status);
        return workOrderRepository.save(newWorkOrder);
    }

    @Transactional
    public WorkOrder updateWorkOrder(WorkOrder workOrder) {

        return workOrderRepository.save(workOrder);
    }











}
