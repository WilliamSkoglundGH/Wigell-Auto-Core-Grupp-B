package com.wac.autocore.service;

import com.wac.autocore.dto.booking.BookingCloneDto;
import com.wac.autocore.exception.BookingNotFoundException;
import com.wac.autocore.mapper.BookingMapper;
import com.wac.autocore.model.*;
import com.wac.autocore.repository.BookingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class BookingService {
    private static final Logger logger = LoggerFactory.getLogger(BookingService.class);
    private final BookingRepository bookingRepository;
    private final MechanicService mechanicService;
    private final VehicleService vehicleService;

    public BookingService(BookingRepository bookingRepository, MechanicService mechanicService, VehicleService vehicleService) {
        this.bookingRepository = bookingRepository;
        this.mechanicService = mechanicService;
        this.vehicleService = vehicleService;
    }

    @Transactional(readOnly = true)
    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }


    @Transactional(readOnly = true)
    public Booking getBooking(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new BookingNotFoundException("booking.error.not_found"));
    }
    @Transactional
    public Booking getBookingByIdWithDetails(Long id) {
        return bookingRepository.findByIdWithDetails(id)
                  .orElseThrow(() -> new BookingNotFoundException("booking.error.not_found"));
    }

    @Transactional
    public Booking saveBooking(Long vehicleId, LocalDate selectedDate, String description, Long mechanicId, List<ServiceItem> serviceItems) {

        if (serviceItems == null || serviceItems.isEmpty()) {
            throw new IllegalArgumentException(
                    "booking.error.select_services"
            );
        }

        Mechanic mechanic = mechanicService.getMechanic(mechanicId);
        Vehicle vehicle = vehicleService.getVehicle(vehicleId);

        List<BookingServiceItem> items = new ArrayList<>();

        Booking newBooking = new Booking(vehicle, selectedDate, description, mechanic, items);

        for (ServiceItem item : serviceItems) {
            newBooking.addServiceItem(item, item.getPrice(), item.getEstimatedMinutes());
        }

        return bookingRepository.save(newBooking);
    }

    @Transactional
    public Booking addServiceItemToBooking(Long bookingId, ServiceItem selectedServiceItem) {

        Booking bookingForUpdate = bookingRepository.findById(bookingId).orElseThrow(() ->
                new BookingNotFoundException("booking.error.not_found"));

        if (selectedServiceItem == null) {
            throw new IllegalArgumentException("booking.error.select_services");
        }

        if (!bookingForUpdate.getStatus().canAddServiceItem()) {
            throw new IllegalStateException("booking.error.not_editable");
        }
        bookingForUpdate.addServiceItem(selectedServiceItem, selectedServiceItem.getPrice(), selectedServiceItem.getEstimatedMinutes());

        return bookingRepository.save(bookingForUpdate);

    }

    @Transactional(readOnly = true)
    public List<Booking> getAllBookingsWithServiceItems() {
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

        return bookingRepository.save(bookingForUpdate);
    }


    public BookingCloneDto cloneBooking(Long id) {
        Booking copiedBooking= getBookingByIdWithDetails(id);
       return BookingMapper.toCloneDto(copiedBooking);
    }



}
