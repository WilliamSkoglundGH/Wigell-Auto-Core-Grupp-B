package com.wac.autocore.service;

import com.wac.autocore.dto.booking.*;
import com.wac.autocore.dto.serviceItem.ServiceItemSummaryDto;
import com.wac.autocore.exception.EntityNotFoundException;
import com.wac.autocore.exception.BookingWrongStatusException;
import com.wac.autocore.mapper.BookingMapper;
import com.wac.autocore.model.*;
import com.wac.autocore.repository.BookingRepository;
import com.wac.autocore.repository.WorkOrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    @Transactional(readOnly = true)
    public List<BookingSummaryDto> getAllBookings() {
        return BookingMapper.toBookingSummaryDtoList(bookingRepository.findAll());
    }

//todo skapa en hjälpmetod så workorder kan plocka entity dirr till sinn planned servicemetod.
    @Transactional(readOnly = true)
    public BookingSummaryDto getBooking(Long id) {
        Booking found = bookingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("booking.error.not_found"));

        return BookingMapper.toBookingSummaryDto(found);
    }
    @Transactional
    public BookingFullDto getBookingByIdWithDetails(Long id) {
         Booking found = bookingRepository.findByIdWithDetails(id)
                  .orElseThrow(() -> new EntityNotFoundException("booking.error.not_found"));
         //TODO Skapa metod bestäm vad som måste finnas med.
        return BookingMapper.toBookingFullDto(found);
    }

    @Transactional
    public BookingSummaryDto saveBooking(BookingCreateDto createDto, List<ServiceItemSummaryDto> serviceItems) {

        if (serviceItems == null || serviceItems.isEmpty()) {
            throw new IllegalArgumentException(
                    "booking.error.select_services"
            );
        }
        Mechanic mechanic = mechanicService.getMechanicEntity(createDto.getMechanicId());
        Vehicle vehicle = vehicleService.getVehicleEntity(createDto.getVehicleId());

        List<Long> ids = serviceItems.stream()
                .map(ServiceItemSummaryDto::getId)
                .collect(Collectors.toList());

        List<ServiceItem> foundServiceItems = serviceItemService.getAllServiceItemsEntity(ids);

        List<BookingServiceItem> items = new ArrayList<>();

        Booking newBooking = new Booking(vehicle, createDto.getDate(), createDto.getDescription(), mechanic, items);

        for (ServiceItem item : foundServiceItems) {
            newBooking.addServiceItem(item, item.getPrice(), item.getEstimatedMinutes());
        }

        bookingRepository.save(newBooking);

        return BookingMapper.toBookingSummaryDto(newBooking);

    }

    @Transactional
    public void addServiceItemToBooking(Long bookingId, ServiceItemSummaryDto selectedServiceItem) {
        Booking bookingForUpdate = bookingRepository.findById(bookingId).orElseThrow(() ->
                new EntityNotFoundException("booking.error.not_found"));

        if (selectedServiceItem == null) {
            throw new IllegalArgumentException("booking.error.select_services");
        }

        if (!bookingForUpdate.getStatus().canAddServiceItem()) {
            throw new IllegalStateException("booking.error.not_editable");
        }
        ServiceItem foundServiceItem = serviceItemService.getServiceItemEntity(selectedServiceItem.getId());

        bookingForUpdate.addServiceItem(foundServiceItem, selectedServiceItem.getPrice(), selectedServiceItem.getEstimatedMinutes());

        syncPlannedWorkOrder(bookingForUpdate);
        bookingRepository.save(bookingForUpdate);
    }

    @Transactional(readOnly = true)
    public List<BookingFullDto> getAllBookingsWithServiceItems() {
      return bookingRepository.findAllWithServiceItems().stream().map(BookingMapper::toBookingFullDto).collect(Collectors.toList());

    }

    @Transactional
    public void removeServiceItemFromBooking(Long bookingId, Long bookingServiceItemId) {
        Booking bookingForUpdate = bookingRepository.findById(bookingId).orElseThrow(() ->
                new EntityNotFoundException("booking.error.not_found"));

        if (!bookingForUpdate.getStatus().canRemoveServiceItem()) {
            throw new IllegalStateException("booking.error.not_editable");
        }
        BookingServiceItem itemToRemove = bookingForUpdate.getServiceItems().stream()
                .filter(item -> item.getId().equals(bookingServiceItemId)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("booking.error.service_not_found"));

        if (bookingForUpdate.getServiceItems().size() <= 1) {
            throw new IllegalStateException("booking.error.select_services");
        }

        bookingForUpdate.getServiceItems().remove(itemToRemove);

        syncPlannedWorkOrder(bookingForUpdate);
        bookingRepository.save(bookingForUpdate);

    }

    @Transactional
    public BookingCloneDto cloneBooking(Long id) {
        BookingFullDto copiedBooking= getBookingByIdWithDetails(id);
        if(!copiedBooking.getStatus().canClone()) {
            throw new BookingWrongStatusException("booking.error.booking_state_completed");
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
        return bookingRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("booking.error.not_found"));
    }



}
