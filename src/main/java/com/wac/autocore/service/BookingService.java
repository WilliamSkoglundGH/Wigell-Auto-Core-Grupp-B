package com.wac.autocore.service;

import com.wac.autocore.dto.booking.BookingCloneDto;
import com.wac.autocore.dto.booking.BookingFullDto;
import com.wac.autocore.dto.booking.BookingSummaryDto;
import com.wac.autocore.dto.mechanic.MechanicSummaryDto;
import com.wac.autocore.dto.serviceItem.ServiceItemSummaryDto;
import com.wac.autocore.exception.BookingNotFoundException;
import com.wac.autocore.mapper.BookingMapper;
import com.wac.autocore.model.*;
import com.wac.autocore.model.enums.BookingState;
import com.wac.autocore.repository.BookingRepository;
import com.wac.autocore.repository.WorkOrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.sun.imageio.plugins.common.I18N.getString;

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
        return bookingRepository.findAll();
    }


    @Transactional(readOnly = true)
    public BookingSummaryDto getBooking(Long id) {
        Booking found = bookingRepository.findById(id)
                .orElseThrow(() -> new BookingNotFoundException("booking.error.not_found"));

        return BookingMapper.toBookingSummaryDto(found);
    }
    @Transactional
    public BookingFullDto getBookingByIdWithDetails(Long id) {
         Booking found = bookingRepository.findByIdWithDetails(id)
                  .orElseThrow(() -> new BookingNotFoundException("booking.error.not_found"));
         //TODO Skapa metod bestäm vad som måste finnas med.
        return BookingMapper.toBookingFullDto(found);
    }

    @Transactional
    public BookingSummaryDto saveBooking(Long vehicleId, LocalDate selectedDate, String description, Long mechanicId, List<ServiceItemSummaryDto> serviceItems) {

        if (serviceItems == null || serviceItems.isEmpty()) {
            throw new IllegalArgumentException(
                    "booking.error.select_services"
            );
        }
        Mechanic mechanic = mechanicService.getMechanicEntity(mechanicId);
        Vehicle vehicle = vehicleService.getVehicle(vehicleId);

        List<Long> ids = serviceItems.stream()
                .map(ServiceItemSummaryDto::getId)
                .collect(Collectors.toList());

        List<ServiceItem> foundServiceItems = serviceItemService.getAllServiceItemsEntity(ids);

        List<BookingServiceItem> items = new ArrayList<>();

        Booking newBooking = new Booking(vehicle, selectedDate, description, mechanic, items);

        for (ServiceItem item : foundServiceItems) {
            newBooking.addServiceItem(item, item.getPrice(), item.getEstimatedMinutes());
        }

        bookingRepository.save(newBooking);

        return BookingMapper.toBookingSummaryDto(newBooking);


    }

    @Transactional
    public BookingSummaryDto addServiceItemToBooking(Long bookingId, ServiceItemSummaryDto selectedServiceItem) {
    //Behöver nått returneras? Vad kommer in i denna TODO Hämta serviceitems..
        Booking bookingForUpdate = bookingRepository.findById(bookingId).orElseThrow(() ->
                new BookingNotFoundException("booking.error.not_found"));

        if (selectedServiceItem == null) {
            throw new IllegalArgumentException("booking.error.select_services");
        }

        if (!bookingForUpdate.getStatus().canAddServiceItem()) {
            throw new IllegalStateException("booking.error.not_editable");
        }
        bookingForUpdate.addServiceItem(selectedServiceItem, selectedServiceItem.getPrice(), selectedServiceItem.getEstimatedMinutes());

        syncPlannedWorkOrder(bookingForUpdate);
        bookingRepository.save(bookingForUpdate);
        return BookingMapper.toBookingSummaryDto(bookingForUpdate);

    }

    @Transactional(readOnly = true)
    public List<BookingSummaryDto> getAllBookingsWithServiceItems() {
        //TODO Mappa om till DTO
        return bookingRepository.findAllWithServiceItems();
    }

    @Transactional
    public Booking removeServiceItemFromBooking(Long bookingId, Long bookingServiceItemId) {
        Booking bookingForUpdate = bookingRepository.findById(bookingId).orElseThrow(() ->
                new BookingNotFoundException("booking.error.not_found"));

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
        return bookingRepository.save(bookingForUpdate);
    }

    @Transactional
    public BookingCloneDto cloneBooking(Long id) {
        Booking copiedBooking= getBookingByIdWithDetails(id);
        if(copiedBooking.getStatus() != BookingState.COMPLETED) {
            throw new IllegalStateException("här");
        }
       return BookingMapper.toCloneDto(copiedBooking);
    }

    //TODO Vad gör denna?
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



}
