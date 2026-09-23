package com.wac.autocore.service;

import com.wac.autocore.model.Booking;
import com.wac.autocore.model.ServiceItem;
import com.wac.autocore.model.WorkOrder;
import com.wac.autocore.repository.WorkOrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public class WorkOrderService {
    private static final Logger logger = LoggerFactory.getLogger(WorkOrderService.class);
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


    @Transactional
    public WorkOrder getWorkOrderById(long id) {
        return workOrderRepository.getWorkOrderById(id);
    }

    //Create
    // Skapa en ny WORKORDER - BOOKING - SERVICEITEM och STATUS - STATUS CREATED - SÄTT BOOKINGS status- WORK_ORDER CREATED

    @Transactional
    public WorkOrder saveWorkOrder(Booking booking, List<ServiceItem> serviceItems, String status) {

        //Lägg till serviceItems i listan på Workorder.;

        WorkOrder newWorkOrder = new WorkOrder(booking, serviceItems, status);
        return workOrderRepository.save(newWorkOrder);

    }


    //PATCH ELLER UPDATE?
    // STARTA WORKORDER-
    // Hämta specifik workorder
    // Ädra workorder till IN_PROGRESS.
    // Hämta specifik mekaniker och sätt som UNAVALEBLE

    //COMPLETE WORKORDER
    // Hämta specifik workorder
    // Ändra workorder till COMPLETED.
    // Hämta specifik mekaniker och sätt som AVALABLE









}
