package com.wac.autocore.service;

import com.wac.autocore.dto.booking.BookingSummaryDto;
import com.wac.autocore.dto.mechanic.MechanicSummaryDto;
import com.wac.autocore.dto.workorder.WorkOrderForMechanicDto;
import com.wac.autocore.exception.MechanicNotFoundException;
import com.wac.autocore.mapper.BookingMapper;
import com.wac.autocore.mapper.MechanicMapper;
import com.wac.autocore.mapper.WorkOrderMapper;
import com.wac.autocore.model.Booking;
import com.wac.autocore.model.Mechanic;
import com.wac.autocore.model.WorkOrder;
import com.wac.autocore.repository.MechanicRepository;
import com.wac.autocore.repository.WorkOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MechanicService {
    private final MechanicRepository mechanicRepository;
    private final WorkOrderRepository workOrderRepository;

    public MechanicService(MechanicRepository mechanicRepository,
                           WorkOrderRepository workOrderRepository) {
        this.mechanicRepository = mechanicRepository;
        this.workOrderRepository = workOrderRepository;
    }

    @Transactional(readOnly = true)
    public List<MechanicSummaryDto> getAllMechanics() {
        return MechanicMapper.toSummaryDtoList(mechanicRepository.findAll());
    }

    @Transactional(readOnly = true)
    public MechanicSummaryDto getMechanic(Long id) {
        return MechanicMapper.toSummaryDto(getMechanicEntity(id));
    }

    @Transactional(readOnly = true)
    public List<BookingSummaryDto> getBookingsForMechanic(Long mechanicId) {
        Mechanic mechanic = getMechanicEntity(mechanicId);

        List<Booking> bookings = mechanic.getBookings();
        if (bookings == null) {
            return Collections.emptyList();
        }

        bookings.sort(Comparator.comparing(Booking::getDate));

        return BookingMapper.toBookingSummaryDtoList(bookings);
    }

    @Transactional(readOnly = true)
    public List<WorkOrderForMechanicDto> getWorkOrdersForMechanic(Long mechanicId) {
        List<WorkOrder> workOrders = workOrderRepository.findByMechanic_Id(mechanicId);

        return workOrders.stream()
                .map(WorkOrderMapper::toMechanicDto)
                .collect(Collectors.toList());
    }

    private Mechanic getMechanicEntity(Long id) {
        return mechanicRepository.findById(id)
                .orElseThrow(() -> new MechanicNotFoundException("mechanic.error.could_not_load"));
    }
}