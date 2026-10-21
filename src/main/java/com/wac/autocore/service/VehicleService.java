package com.wac.autocore.service;

import com.wac.autocore.dto.vechicle.VehicleCreateDto;
import com.wac.autocore.dto.vechicle.VehicleSummaryDto;
import com.wac.autocore.exception.EntityNotFoundException;
import com.wac.autocore.mapper.VehicleMapper;
import com.wac.autocore.model.Customer;
import com.wac.autocore.model.Vehicle;
import com.wac.autocore.repository.CustomerRepository;
import com.wac.autocore.repository.VehicleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class VehicleService {
    private static final Logger logger = LoggerFactory.getLogger(VehicleService.class);
    private final VehicleRepository vehicleRepository;
    private final CustomerRepository customerRepository;

    public VehicleService(VehicleRepository vehicleRepository, CustomerRepository customerRepository) {
        this.vehicleRepository = vehicleRepository;
        this.customerRepository = customerRepository;
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public List<VehicleSummaryDto> getAllVehicles() {
        return VehicleMapper.toDtoList(vehicleRepository.findAll());
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public VehicleSummaryDto getVehicle(Long id) {
        return VehicleMapper.toDto(vehicleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("vehicle.error.not_found")));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MECHANIC')")
    @Transactional
    public VehicleSummaryDto saveVehicle(VehicleCreateDto dto) {
        // 1. Hämta kund-entiteten säkert via ID
        Customer customer = customerRepository.findById(dto.getCustomerId())
                .orElseThrow(() -> new EntityNotFoundException("customer.error.could_not_load"));

        // 2. Låt mappen skapa entiteten från DTO:n
        Vehicle vehicle = VehicleMapper.toEntity(dto, customer);

        // 3. Spara och returnera som DTO
        Vehicle savedVehicle = vehicleRepository.save(vehicle);
        return VehicleMapper.toDto(savedVehicle);
    }

    protected Vehicle getVehicleEntity(Long vehicleId) {
        return vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new EntityNotFoundException("vehicle.error.not_found"));
    }
}
