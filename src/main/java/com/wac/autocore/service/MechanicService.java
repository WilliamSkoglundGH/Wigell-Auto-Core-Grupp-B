package com.wac.autocore.service;

import com.wac.autocore.exception.MechanicNotFoundException;
import com.wac.autocore.model.Booking;
import com.wac.autocore.model.Mechanic;
import com.wac.autocore.model.WorkOrder;
import com.wac.autocore.repository.MechanicRepository;
import com.wac.autocore.repository.WorkOrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MechanicService {
    private static final Logger logger = LoggerFactory.getLogger(MechanicService.class);
    private final MechanicRepository mechanicRepository;
    private final WorkOrderRepository workOrderRepository;


    public MechanicService(MechanicRepository mechanicRepository,
                           WorkOrderRepository workOrderRepository) {
        this.mechanicRepository = mechanicRepository;
        this.workOrderRepository = workOrderRepository;
    }

    @Transactional(readOnly = true)
    public List<Mechanic> getAllMechanics() {
        return mechanicRepository.findAll();
    }
    @Transactional(readOnly = true)
    public Mechanic getMechanic(Long id) {
        return mechanicRepository.findById(id)
                .orElseThrow(() -> new MechanicNotFoundException("Mechanic with ID " + id + " not found."));
    }

    @Transactional(readOnly = true)
    public List<Booking> getBookingsForMechanic(Long mechanicId) {

        Mechanic mechanic = getMechanic(mechanicId);

        List<Booking> bookings = mechanic.getBookings();

        bookings.sort((a, b) -> a.getDate().compareTo(b.getDate()));

        return bookings;
    }

    @Transactional(readOnly = true)
    public List<WorkOrder> getWorkOrdersForMechanic(Long mechanicId) {
        return workOrderRepository.findByBooking_Mechanic_Id(mechanicId);
    }





}
