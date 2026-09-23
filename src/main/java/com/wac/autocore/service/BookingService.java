package com.wac.autocore.service;

import com.wac.autocore.model.Booking;
import com.wac.autocore.model.Mechanic;
import com.wac.autocore.model.Vehicle;
import com.wac.autocore.repository.BookingRepository;
import com.wac.autocore.repository.MechanicRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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

    //bookingService.saveBooking(vehicleId, selectedDate, description, mechanicId);
    @Transactional
    public Booking saveBooking(Long vehicleId, LocalDate selectedDate, String description, Long mechanicId) {
        Mechanic mechanic = mechanicService.getMechanic(mechanicId);
        Vehicle vehicle = vehicleService.getVehicle(vehicleId);
        Booking booking = new Booking(vehicle, selectedDate, description, "BOOKED", mechanic);

        return bookingRepository.save(booking);
    }
    }
