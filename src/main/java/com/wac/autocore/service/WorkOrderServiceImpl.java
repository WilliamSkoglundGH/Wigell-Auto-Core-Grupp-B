package com.wac.autocore.service;

import com.wac.autocore.dto.serviceItem.ServiceItemSummaryDto;
import com.wac.autocore.dto.workorder.*;
import com.wac.autocore.exception.*;
import com.wac.autocore.service.workorder.WorkOrderFactory;
import com.wac.autocore.mapper.WorkOrderMapper;
import com.wac.autocore.model.*;
import com.wac.autocore.model.enums.BookingState;
import com.wac.autocore.model.enums.WorkOrderState;
import com.wac.autocore.repository.WorkOrderRepository;
import org.hibernate.jdbc.Work;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class WorkOrderServiceImpl implements WorkOrderService {

    private final Logger logger = LoggerFactory.getLogger(WorkOrderServiceImpl.class);
    private WorkOrderRepository workOrderRepository;
    private BookingService bookingService;
    private MechanicService mechanicService;
    private VehicleService vehicleService;
    private ServiceItemService serviceItemService;
    private WorkOrderFactory workOrderFactory;

    public WorkOrderServiceImpl(WorkOrderRepository workOrderRepository, BookingService bookingService, MechanicService mechanicService,
                            VehicleService vehicleService, ServiceItemService serviceItemService, WorkOrderFactory workOrderFactory) {
        this.workOrderRepository = workOrderRepository;
        this.bookingService = bookingService;
        this.mechanicService = mechanicService;
        this.vehicleService = vehicleService;
        this.serviceItemService = serviceItemService;
        this.workOrderFactory = workOrderFactory;
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkOrderSummaryDto> getAllWorkOrders() {
        List<WorkOrder> workOrders = workOrderRepository.findAll();

        return workOrders.stream()
                .map(WorkOrderMapper::toSummaryDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkOrderForInvoiceDto> getAllWorkOrdersForInvoice() {
        List<WorkOrder> workOrders = workOrderRepository.findAll();

        return workOrders.stream()
                .map(WorkOrderMapper::toInvoiceViewDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public WorkOrderResponseDto getWorkOrderById(long id) {

        WorkOrder workOrder = workOrderRepository.findById(id).orElseThrow(() -> new EntityNotFoundException(
                "workorder.error.work_order_not_found"));

        return WorkOrderMapper.toResponseDto(workOrder,
                WorkOrderMapper.toServiceItemDtoList(workOrder.getServiceItems()));

    }

    @Override
    @Transactional
    public WorkOrderSummaryDto createPlannedWorkOrder(Long bookingId) {
        if (bookingId == null) {
            throw new EntityNotFoundException("booking.error.not_found");
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

    @Override
    @Transactional
    public WorkOrderSummaryDto createDropInWorkOrder(WorkOrderCreateDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Workorder data is required");
        }

        if (dto.getVehicleId() == null) {
            throw new EntityNotFoundException("workorder.error.select_vehicle");
        }

        Vehicle vehicle = vehicleService.getVehicleEntity(dto.getVehicleId());

        Mechanic mechanic = dto.getMechanicId() != null ? mechanicService.getMechanicEntity(dto.getMechanicId()) : null;

        List<ServiceItem> serviceItems = serviceItemService.getAllServiceItemsEntity(dto.getServiceItemsIds());

        WorkOrder workOrderDropIn = workOrderFactory.createDropIn(dto, vehicle, mechanic, serviceItems);

        WorkOrder savedWorkOrder = workOrderRepository.save(workOrderDropIn);
        return WorkOrderMapper.toSummaryDto(savedWorkOrder);
    }

    @Override
    @Transactional
    public WorkOrderSummaryDto createClaimWorkOrder(Long originalWorkOrderId, WorkOrderCreateDto dto) {
        try {
            if (originalWorkOrderId == null) {
                throw new EntityNotFoundException("workorder.error.work_order_not_found");
            }

            WorkOrder workOrderOriginal = workOrderRepository.findById(originalWorkOrderId).orElseThrow(
                    () -> {
                        logger.error("Method: createClaimWorkOrder | No workorder found | Id: {} ", originalWorkOrderId);
                        return new EntityNotFoundException("workorder.error.work_order_not_found");
                    });

            if (workOrderOriginal.getStatus() != WorkOrderState.COMPLETED) {
                throw new WorkOrderWrongStatusException("workorder.error.claim_requires_completed");
            }

            WorkOrder workOrderClaim = workOrderFactory.createClaim(workOrderOriginal, dto);
            WorkOrder saved = workOrderRepository.save(workOrderClaim);
            logger.info("Method: createInvoice | Success | WorkorderId: {}", saved.getId());
            return WorkOrderMapper.toSummaryDto(saved);
        } catch (DataAccessException e) {
            logger.error("Method: createClaimWorkOrder | Database error ", e);
            throw new DatabaseException("common.error.database");
        }
    }

    @Override
    @Transactional
    public void startWorkOrder(Long workOrderId) {
        try {
            if (workOrderId == null) {
                logger.error("Method: startWorkOrder | No workorder found | Id: null");
                throw new EntityNotFoundException("workorder.error.work_order_not_found");
            }
            WorkOrder workOrder = workOrderRepository.findById(workOrderId)
                    .orElseThrow(() -> {
                        logger.warn("Method: startWorkOrder | No workorder found | Id: {} ", workOrderId);
                        return new EntityNotFoundException("workorder.error.work_order_not_found");
                    });

            if (!workOrder.getStatus().canStart()) {
                logger.warn("Method: startWorkOrder | Workorder wrong status | Id: {} ", workOrderId);
                throw new WorkOrderWrongStatusException("workorder.error.work_order_wrong_status");
            }

            Mechanic mechanic = mechanicService.getMechanicEntity(workOrder.getMechanic().getId());
            if (!mechanic.isAvailable()) {
                logger.warn("Method: startWorkOrder | Mechanic not available | Id: {} ", mechanic.getId());
                throw new MechanicNotAvailableException("workorder.error.mechanic_not_available");
            }

            if (workOrder.getServiceItems().isEmpty()) {
                logger.warn("Method: startWorkOrder | No service items | workorderId: {} ", workOrder.getId());
                throw new BookingWithoutServicesException("serviceItem.error.no_service_items");
            }

            workOrder.setStartTime(LocalDateTime.now());
            workOrder.setStatus(workOrder.getStatus().getNext());
            mechanic.setAvailable(false);

            Booking booking = workOrder.getBooking();
            if (booking != null) {
                booking.setStatus(booking.getStatus().getNext());
            }

            WorkOrder saved = workOrderRepository.save(workOrder);
            logger.info("Method: startWorkOrder | Success | WorkorderId: {}", saved.getId());

        } catch (DataAccessException e) {
            logger.error("Method: createClaimWorkOrder | Database error ", e);
            throw new DatabaseException("common.error.database");
        }
    }

    @Override
    @Transactional
    public void completeWorkOrder(Long workOrderId) {
        try {
            if (workOrderId == null) {
                logger.warn("Method: completeWorkOrder | Workorder null | Id: null");
                throw new EntityNotFoundException("workorder.error.work_order_not_found");
            }
            WorkOrder workOrder = workOrderRepository.findById(workOrderId)
                    .orElseThrow(() -> {
                        logger.warn("Method: completeWorkOrder | Workorder not found | workorderId: {} ", workOrderId);

                        return new EntityNotFoundException("workorder.error.work_order_not_found");
                    });

            if (!workOrder.getStatus().canComplete()) {
                logger.warn("Method: completeWorkOrder | Workorder wrong status | workorderId: {} ", workOrderId);
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

            WorkOrder saved = workOrderRepository.save(workOrder);
            logger.info("Method: completeWorkOrder | Success | WorkorderId: {}", saved.getId());
        } catch (DataAccessException e) {
            logger.error("Method: completeWorkOrder | Database error ", e);
            throw new DatabaseException("common.error.database");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public WorkOrderDetailsDto getWorkOrderInfoById(Long workOrderId) {
        try {
            if (workOrderId == null) {
                throw new EntityNotFoundException("workorder.error.work_order_not_found");
            }

            WorkOrder workOrder = workOrderRepository.findById(workOrderId)
                    .orElseThrow(() -> new EntityNotFoundException("workorder.error.work_order_not_found"));

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
        } catch (DataAccessException e) {
            logger.error("Method: getWorkOrderInfoById | Database error ", e);
            throw new DatabaseException("common.error.database");
        }
    }

    @Override
    @Transactional
    public void confirmWorkOrder(Long workOrderId) {
        try {
            if (workOrderId == null) {
                logger.warn("Method: completeWorkOrder | Workorder not found | workorderId = null");
                throw new EntityNotFoundException("workorder.error.work_order_not_found");
            }

            WorkOrder workOrder = workOrderRepository.findById(workOrderId)
                    .orElseThrow(() -> {
                        logger.warn("Method: completeWorkOrder | Workorder not found | workorderId: {} ", workOrderId);
                        return new EntityNotFoundException("workorder.error.work_order_not_found");
                    });

            if (!workOrder.getStatus().canConfirm()) {
                logger.warn("Method: confirmWorkOrder | Workorder wrong status | workorderId: {} ", workOrderId);
                throw new WorkOrderWrongStatusException("workorder.error.work_order_wrong_status");
            }

            if (!workOrder.isReadyToConfirm()) {
                logger.warn("Method: confirmWorkOrder | Workorder wrong status | workorderId: {} ", workOrderId);
                throw new WorkOrderWrongStatusException("workorder.error.not_ready_to_confirm");
            }

            workOrder.setStatus(workOrder.getStatus().getNext());
            WorkOrder saved = workOrderRepository.save(workOrder);
            logger.info("Method: confirmWorkOrder | Success | WorkorderId: {}", saved.getId());
        } catch (DataAccessException e) {
            logger.error("Method: getWorkOrderInfoById | Database error ", e);
            throw new DatabaseException("common.error.database");
        }
    }

    @Override
    @Transactional
    public void cancelWorkOrder(Long workOrderId) {
        try {
            if (workOrderId == null) {
                logger.warn("Method: cancelWorkOrder | Workorder not found | workorderId = null");
                throw new EntityNotFoundException("workorder.error.work_order_not_found");
            }

            WorkOrder workOrder = workOrderRepository.findById(workOrderId)

                    .orElseThrow(() -> {
                        logger.warn("Method: cancelWorkOrder | Workorder not found | workorderId: {} ", workOrderId);
                        return new EntityNotFoundException("workorder.error.work_order_not_found");
                    });

            if (!workOrder.getStatus().canCancel()) {
                logger.warn("Method: cancelWorkOrder | Workorder wrong status | workorderId: {} ", workOrderId);
                throw new WorkOrderWrongStatusException(
                        "workorder.error.work_order_wrong_status");
            }

            workOrder.setStatus(WorkOrderState.CANCELED);
            WorkOrder w = workOrderRepository.save(workOrder);
            logger.info("Method: cancelWorkOrder | Success | WorkorderId: {}", w.getId());
        } catch (DataAccessException e) {
            logger.error("Method: cancelWorkOrder | Database error ", e);
            throw new DatabaseException("common.error.database");
        }
    }

    @Override
    @Transactional
    public void addServiceItemToWorkOrder(Long workOrderId, Long serviceItemId) {
        try {
            if (workOrderId == null) {
                logger.warn("Method: addServiceItemToWorkOrder | Workorder not found | workorderId = null");
                throw new EntityNotFoundException("workorder.error.work_order_not_found");
            }

            if (serviceItemId == null) {
                logger.warn("Method: addServiceItemToWorkOrder | No service items | workorderId: {} ", workOrderId);
                throw new EntityNotFoundException("serviceItem.error.no_service_items");
            }

            WorkOrder workOrder = workOrderRepository.findById(workOrderId)
                    .orElseThrow(() -> {
                        logger.warn("Method: addServiceItemToWorkOrder | Workorder not found | workorderId: {} ", workOrderId);
                        return new EntityNotFoundException("workorder.error.work_order_not_found");
                    });

            if (!workOrder.getStatus().canEdit()) {
                logger.warn("Method: addServiceItemToWorkOrder | Workorder not editable | workorderId: {} ", workOrderId);
                throw new WorkOrderWrongStatusException("workorder.error.not_editable");
            }

            if (workOrder.getBooking() != null) {
                logger.warn("Method: addServiceItemToWorkOrder | Booking not found | workorderId: {} ", workOrderId);
                throw new WorkOrderWrongStatusException("workorder.error.planned_workorder");
            }

            ServiceItem serviceItem = serviceItemService.getServiceItemEntity(serviceItemId);
            workOrder.addServiceItem(serviceItem, serviceItem.getPrice(), serviceItem.getEstimatedMinutes());
            WorkOrder w = workOrderRepository.save(workOrder);
            logger.info("Method: addServiceItemToWorkOrder | Success | WorkorderId: {}", w.getId());
        } catch (DataAccessException e) {
            logger.error("Method: addServiceItemToWorkOrder | Database error ", e);
            throw new DatabaseException("common.error.database");
        }
    }

    @Override
    @Transactional
    public void removeServiceItemFromWorkOrder(Long workOrderId, Long workOrderServiceItemId) {
        try {
            if (workOrderId == null) {
                logger.warn("Method: removeServiceItemFromWorkOrder | Workorder not found | workorderId = null");
                throw new EntityNotFoundException("workorder.error.work_order_not_found");
            }

            if (workOrderServiceItemId == null) {
                logger.warn("Method: removeServiceItemFromWorkOrder | No service items | workorderId: {} ", workOrderId);
                throw new EntityNotFoundException("serviceItem.error.no_service_items");
            }

            WorkOrder workOrder = workOrderRepository.findById(workOrderId)
                    .orElseThrow(() -> {
                        logger.warn("Method: removeServiceItemFromWorkOrder | Workorder not found | workorderId: {} ", workOrderId);
                        return new EntityNotFoundException("workorder.error.work_order_not_found");
                    });

            if (!workOrder.getStatus().canEdit()) {
                logger.warn("Method: removeServiceItemFromWorkOrder | Workorder not editable | workorderId: {} ", workOrderId);
                throw new WorkOrderWrongStatusException("workorder.error.not_editable");
            }

            if (workOrder.getBooking() != null) {
                logger.warn("Method: removeServiceItemFromWorkOrder | Workorder not editable | workorderId: {} ", workOrderId);
                throw new WorkOrderWrongStatusException("workorder.error.planned_workorder");
            }

            WorkOrderServiceItem itemToRemove = workOrder.getServiceItems().stream()
                    .filter(item -> item.getId().equals(workOrderServiceItemId))
                    .findFirst()
                    .orElseThrow(() -> new EntityNotFoundException("serviceItem.error.could_not_load"));

            workOrder.getServiceItems().remove(itemToRemove);
            WorkOrder w = workOrderRepository.save(workOrder);
            logger.info("Method: removeServiceItemFromWorkOrder | Success | WorkorderId: {}", w.getId());
        } catch (DataAccessException e) {
            logger.error("Method: removeServiceItemFromWorkOrder | Database error ", e);
            throw new DatabaseException("common.error.database");
        }
    }

    @Override
    @Transactional
    public void updateWorkOrder(Long workOrderId, WorkOrderCreateDto dto) {
        try {
            if (workOrderId == null) {
                logger.warn("Method: updateWorkOrder | Workorder not found | workorderId = null");
                throw new EntityNotFoundException("workorder.error.work_order_not_found");
            }

            if (dto == null) {
                logger.warn("Method: updateWorkOrder | Dto not found | workorderId: {} ", workOrderId);
                throw new IllegalArgumentException("Update data is required.");
            }

            WorkOrder workOrder = workOrderRepository.findById(workOrderId)
                    .orElseThrow(() -> {
                        logger.warn("Method: updateWorkOrder | Workorder not found | workorderId: {} ", workOrderId);
                        return new EntityNotFoundException("workorder.error.work_order_not_found");
                    });

            if (!workOrder.getStatus().canEdit()) {
                logger.warn("Method: updateWorkOrder | Workorder not editable | workorderId: {} ", workOrderId);
                throw new WorkOrderWrongStatusException("workorder.error.not_editable");
            }

            if (workOrder.getBooking() != null) {
                logger.warn("Method: updateWorkOrder | Workorder not editable | workorderId: {} ", workOrderId);
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

            WorkOrder w = workOrderRepository.save(workOrder);
            logger.info("Method: removeServiceItemFromWorkOrder | Success | WorkorderId: {}", w.getId());
        } catch (DataAccessException e) {
            logger.error("Method: removeServiceItemFromWorkOrder | Database error ", e);
            throw new DatabaseException("common.error.database");
        }
    }
}

