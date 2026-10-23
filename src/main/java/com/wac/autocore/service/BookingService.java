package com.wac.autocore.service;

import com.wac.autocore.dto.booking.*;
import com.wac.autocore.dto.serviceItem.ServiceItemSummaryDto;
import com.wac.autocore.exception.DatabaseException;
import com.wac.autocore.exception.EntityNotFoundException;
import com.wac.autocore.exception.BookingWrongStatusException;
import com.wac.autocore.mapper.BookingMapper;
import com.wac.autocore.model.*;
import com.wac.autocore.repository.BookingRepository;
import com.wac.autocore.repository.WorkOrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookingService {
    private static final Logger logger = LoggerFactory.getLogger(BookingService.class);
    private final BookingRepository bookingRepository;
    private final MechanicService mechanicService;
    private final VehicleService vehicleService;
    private final ServiceItemService serviceItemService;
    private final WorkOrderRepository workOrderRepository;

    public BookingService(BookingRepository bookingRepository, MechanicService mechanicService, VehicleService vehicleService,
                          WorkOrderRepository workOrderRepository,ServiceItemService serviceItemService) {
        this.bookingRepository = bookingRepository;
        this.mechanicService = mechanicService;
        this.vehicleService = vehicleService;
        this.workOrderRepository = workOrderRepository;
        this.serviceItemService = serviceItemService;
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public List<BookingSummaryDto> getAllBookings() {

        try {

            return BookingMapper.toBookingSummaryDtoList(
                    bookingRepository.findAll());

        } catch (DataAccessException e) {

            logger.error(
                    "Method: getAllBookings | Database error",
                    e);

            throw new DatabaseException(
                    "common.error.database");
        }
    }

    @PreAuthorize("isAuthenticated()")
    public BookingSummaryDto getBooking(Long id) {

        try {

            Booking found = bookingRepository.findById(id)
                    .orElseThrow(() -> {

                        logger.warn(
                                "Method: getBooking | No booking found | Id:{}",
                                id);

                        return new EntityNotFoundException(
                                "booking.error.not_found");
                    });

            return BookingMapper.toBookingSummaryDto(found);

        } catch (DataAccessException e) {

            logger.error(
                    "Method: getBooking | Database error",
                    e);

            throw new DatabaseException(
                    "common.error.database");
        }
    }

    public BookingFullDto getBookingByIdWithDetails(Long id) {

        try {

            Booking found = bookingRepository.findByIdWithDetails(id)
                    .orElseThrow(() -> {

                        logger.warn(
                                "Method: getBookingByIdWithDetails | No booking found | Id:{}",
                                id);

                        return new EntityNotFoundException(
                                "booking.error.not_found");
                    });

            return BookingMapper.toBookingFullDto(found);

        } catch (DataAccessException e) {

            logger.error(
                    "Method: getBookingByIdWithDetails | Database error",
                    e);

            throw new DatabaseException(
                    "common.error.database");
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public BookingSummaryDto saveBooking(
            BookingCreateDto createDto,
            List<ServiceItemSummaryDto> serviceItems) {

        if (serviceItems == null || serviceItems.isEmpty()) {

            logger.warn(
                    "Method: saveBooking | ServiceItems is null or empty");

            throw new IllegalArgumentException(
                    "booking.error.select_services");
        }

        Mechanic mechanic =
                mechanicService.getMechanicEntity(
                        createDto.getMechanicId());

        Vehicle vehicle =
                vehicleService.getVehicleEntity(
                        createDto.getVehicleId());

        List<Long> ids = serviceItems.stream()
                .map(ServiceItemSummaryDto::getId)
                .collect(Collectors.toList());

        List<ServiceItem> foundServiceItems =
                serviceItemService.getAllServiceItemsEntity(ids);

        List<BookingServiceItem> items = new ArrayList<>();

        Booking newBooking =
                new Booking(
                        vehicle,
                        createDto.getDate(),
                        createDto.getDescription(),
                        mechanic,
                        items);

        for (ServiceItem item : foundServiceItems) {

            newBooking.addServiceItem(
                    item,
                    item.getPrice(),
                    item.getEstimatedMinutes());
        }

        try {

            Booking savedBooking =
                    bookingRepository.save(newBooking);

            logger.info(
                    "Method: saveBooking | Success | BookingId:{}",
                    savedBooking.getId());

            return BookingMapper.toBookingSummaryDto(
                    savedBooking);

        } catch (DataAccessException e) {

            logger.error(
                    "Method: saveBooking | Database error",
                    e);

            throw new DatabaseException(
                    "common.error.database");
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void addServiceItemToBooking(
            Long bookingId,
            ServiceItemSummaryDto selectedServiceItem) {

        Booking bookingForUpdate =
                bookingRepository.findById(bookingId)
                        .orElseThrow(() -> {

                            logger.warn(
                                    "Method: addServiceItemToBooking | No Booking found | Id:{}",
                                    bookingId);

                            return new EntityNotFoundException(
                                    "booking.error.not_found");
                        });

        if (selectedServiceItem == null) {

            logger.warn(
                    "Method: addServiceItemToBooking | No Service items selected");

            throw new IllegalArgumentException(
                    "booking.error.select_services");
        }

        if (!bookingForUpdate.getStatus().canAddServiceItem()) {

            logger.warn(
                    "Method: addServiceItemToBooking | Booking has wrong status. Can't be updated");

            throw new IllegalStateException(
                    "booking.error.not_editable");
        }

        ServiceItem foundServiceItem =
                serviceItemService.getServiceItemEntity(
                        selectedServiceItem.getId());

        bookingForUpdate.addServiceItem(
                foundServiceItem,
                selectedServiceItem.getPrice(),
                selectedServiceItem.getEstimatedMinutes());

        syncPlannedWorkOrder(bookingForUpdate);

        try {

            bookingRepository.save(bookingForUpdate);
            logger.info(
                    "Method: addServiceItemToBooking | Success updating | BookingId:{}",
                    bookingId);

        } catch (DataAccessException e) {
            logger.error(
                    "Method: addServiceItemToBooking | Database error | BookingId:{}",
                    bookingId,
                    e);
            throw new DatabaseException(
                    "common.error.database");
        }
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public List<BookingFullDto> getAllBookingsWithServiceItems() {

        try {
            return bookingRepository.findAllWithServiceItems()
                    .stream()
                    .map(BookingMapper::toBookingFullDto)
                    .collect(Collectors.toList());

        } catch (DataAccessException e) {

            logger.error(
                    "Method: getAllBookingsWithServiceItems | Database error",
                    e);

            throw new DatabaseException(
                    "common.error.database");
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void removeServiceItemFromBooking(
            Long bookingId,
            Long bookingServiceItemId) {

        Booking bookingForUpdate =
                bookingRepository.findById(bookingId)
                        .orElseThrow(() -> {

                            logger.warn(
                                    "Method: removeServiceItemFromBooking | No Booking found | Id:{}",
                                    bookingId);

                            return new EntityNotFoundException(
                                    "booking.error.not_found");
                        });

        if (!bookingForUpdate.getStatus().canRemoveServiceItem()) {

            logger.warn(
                    "Method: removeServiceItemFromBooking | Booking has wrong status, cant be updated");

            throw new IllegalStateException(
                    "booking.error.not_editable");
        }

        BookingServiceItem itemToRemove =
                bookingForUpdate.getServiceItems()
                        .stream()
                        .filter(item ->
                                item.getId().equals(bookingServiceItemId))
                        .findFirst()
                        .orElseThrow(() -> {

                            logger.warn(
                                    "Method: removeServiceItemFromBooking | Service item not found");

                            return new IllegalArgumentException(
                                    "booking.error.service_not_found");
                        });

        if (bookingForUpdate.getServiceItems().size() <= 1) {

            logger.warn(
                    "Method: removeServiceItemFromBooking | Booking cant remove last service item");

            throw new IllegalStateException(
                    "booking.error.cant_remove");
        }

        bookingForUpdate.getServiceItems().remove(itemToRemove);

        syncPlannedWorkOrder(bookingForUpdate);

        try {

            bookingRepository.save(bookingForUpdate);

            logger.info(
                    "Method: removeServiceItemFromBooking | Success updating | BookingId:{}",
                    bookingId);

        } catch (DataAccessException e) {

            logger.error(
                    "Method: removeServiceItemFromBooking | Database error | BookingId:{}",
                    bookingId,
                    e);

            throw new DatabaseException(
                    "common.error.database");
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public BookingCloneDto cloneBooking(Long id) {

        BookingFullDto copiedBooking =
                getBookingByIdWithDetails(id);

        if (!copiedBooking.getStatus().canClone()) {

            logger.warn(
                    "Method: cloneBooking | Booking wrong status | Status:{}",
                    copiedBooking.getStatus());

            throw new BookingWrongStatusException(
                    "booking.error.booking_state_completed");
        }

        return BookingMapper.toCloneDto(copiedBooking);
    }

    private void syncPlannedWorkOrder(Booking booking) {
        workOrderRepository.findByBooking_Id(booking.getId()).ifPresent(workOrder -> {
            workOrder.getServiceItems().clear();
            for (BookingServiceItem item : booking.getServiceItems()) {
                workOrder.addServiceItem(
                        item.getServiceItem(),
                        item.getPriceAtTime(),
                        item.getDurationAtTime());
            }
        });
    }

    protected Booking getBookingEntity(Long id) {

        try {

            return bookingRepository.findById(id)
                    .orElseThrow(() -> {

                        logger.warn(
                                "Method: getBookingEntity | No booking found | Id:{}",
                                id);

                        return new EntityNotFoundException(
                                "booking.error.not_found");
                    });

        } catch (DataAccessException e) {

            logger.error(
                    "Method: getBookingEntity | Database error",
                    e);

            throw new DatabaseException(
                    "common.error.database");
        }
    }
}
