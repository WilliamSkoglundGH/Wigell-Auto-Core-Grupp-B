package com.wac.autocore.service;

import com.wac.autocore.model.Vehicle;
import com.wac.autocore.repository.VehicleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class VehicleService {
    private static final Logger logger = LoggerFactory.getLogger(VehicleService.class);
    private final VehicleRepository vehicleRepository;

    public VehicleService(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    @Transactional(readOnly = true)
    public List<Vehicle> getAllVehicles() {
        return vehicleRepository.findAll();
    }
    @Transactional
    public Vehicle saveVehicle(Vehicle vehicle) {
        try {
            return vehicleRepository.save(vehicle);
        } catch (Exception e) {
            logger.error("Could not save vehicle: {}", e.getMessage(), e);
            throw new RuntimeException("Could not save vehicle.", e);
        }
    }
}
