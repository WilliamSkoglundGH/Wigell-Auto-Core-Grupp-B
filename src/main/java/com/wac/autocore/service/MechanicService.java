package com.wac.autocore.service;

import com.wac.autocore.dto.booking.BookingSummaryDto;
import com.wac.autocore.dto.mechanic.MechanicSummaryDto;
import com.wac.autocore.dto.workorder.WorkOrderForMechanicDto;
import com.wac.autocore.exception.DatabaseException;
import com.wac.autocore.exception.EntityNotFoundException;
import com.wac.autocore.mapper.BookingMapper;
import com.wac.autocore.mapper.MechanicMapper;
import com.wac.autocore.mapper.WorkOrderMapper;
import com.wac.autocore.model.Booking;
import com.wac.autocore.model.Mechanic;
import com.wac.autocore.model.WorkOrder;
import com.wac.autocore.repository.MechanicRepository;
import com.wac.autocore.repository.WorkOrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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

    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public List<MechanicSummaryDto> getAllMechanics() {
        try {
            return MechanicMapper.toSummaryDtoList(mechanicRepository.findAll());
        } catch (DataAccessException e) {
            logger.error("Method: getAllMechanics | Database error ", e);
            throw new DatabaseException("common.error.database");
        }
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public List<BookingSummaryDto> getBookingsForMechanic(Long mechanicId) {
        try{
            Mechanic mechanic = getMechanicEntity(mechanicId);

            List<Booking> bookings = mechanic.getBookings();
            if (bookings == null) {
                return Collections.emptyList();
            }

            bookings.sort(Comparator.comparing(Booking::getDate));

            return BookingMapper.toBookingSummaryDtoList(bookings);
        }catch (DataAccessException e) {
            logger.error("Method: getBookingsForMechanic | Database error | MechanicId={}", mechanicId, e);
            throw new DatabaseException("common.error.database");
        }
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public List<WorkOrderForMechanicDto> getWorkOrdersForMechanic(Long mechanicId) {
        try{
            List<WorkOrder> workOrders = workOrderRepository.findByMechanic_Id(mechanicId);

            return workOrders.stream()
                    .map(WorkOrderMapper::toMechanicDto)
                    .collect(Collectors.toList());
        } catch (DataAccessException e) {
            logger.error("Method: getWorkOrdersForMechanic | Database error | MechanicId={}", mechanicId, e);
            throw new DatabaseException("common.error.database");
        }

    }

    public Mechanic getMechanicEntity(Long id) {
        try{
            return mechanicRepository.findById(id)
                    .orElseThrow(() -> {
                        logger.warn("Method: getMechanicEntity | Mechanic not found | MechanicId={}", id);
                        return new EntityNotFoundException("mechanic.error.not_found");
                    });
        }catch (DataAccessException e) {
        logger.error("Method: getMechanicEntity | Database error | MechanicId={}", id, e);
        throw new DatabaseException("common.error.database");
    }
    }
}