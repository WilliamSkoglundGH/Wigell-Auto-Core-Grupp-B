package com.wac.autocore.service;

import com.wac.autocore.dto.vechicle.VehicleCreateDto;
import com.wac.autocore.dto.vechicle.VehicleSummaryDto;
import com.wac.autocore.exception.DatabaseException;
import com.wac.autocore.exception.EntityNotFoundException;
import com.wac.autocore.mapper.VehicleMapper;
import com.wac.autocore.model.Customer;
import com.wac.autocore.model.Vehicle;
import com.wac.autocore.repository.CustomerRepository;
import com.wac.autocore.repository.VehicleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class VehicleService {

    private static final Logger logger =
            LoggerFactory.getLogger(VehicleService.class);

    private final VehicleRepository vehicleRepository;
    private final CustomerRepository customerRepository;

    public VehicleService(
            VehicleRepository vehicleRepository,
            CustomerRepository customerRepository) {

        this.vehicleRepository = vehicleRepository;
        this.customerRepository = customerRepository;
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public List<VehicleSummaryDto> getAllVehicles() {

        try {

            return VehicleMapper.toDtoList(
                    vehicleRepository.findAll());

        } catch (DataAccessException e) {
            logger.error(
                    "Method: getAllVehicles | Database error",
                    e);

            throw new DatabaseException(
                    "common.error.database");
        }
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public VehicleSummaryDto getVehicle(Long id) {

        return VehicleMapper.toDto(
                vehicleRepository.findById(id)
                        .orElseThrow(() -> {

                            logger.warn(
                                    "Method: getVehicle | Could not find vehicle | Id:{}",
                                    id);

                            return new EntityNotFoundException(
                                    "vehicle.error.not_found");
                        }));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MECHANIC')")
    @Transactional
    public VehicleSummaryDto saveVehicle(VehicleCreateDto dto) {

        Customer customer = customerRepository.findById(
                        dto.getCustomerId())
                .orElseThrow(() -> {

                    logger.warn(
                            "Method: saveVehicle | Could not find customer | Id:{}",
                            dto.getCustomerId());

                    return new EntityNotFoundException(
                            "customer.error.could_not_load");
                });

        Vehicle vehicle =
                VehicleMapper.toEntity(dto, customer);

        Vehicle savedVehicle =
                vehicleRepository.save(vehicle);

        logger.info(
                "Method: saveVehicle | Success | VehicleId:{}",
                savedVehicle.getId());

        return VehicleMapper.toDto(savedVehicle);
    }

    protected Vehicle getVehicleEntity(Long vehicleId) {

        return vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> {
                    logger.error(
                            "Method: getVehicleEntity | Could not find vehicle | Id:{}",
                            vehicleId);

                    return new EntityNotFoundException(
                            "vehicle.error.not_found");
                });
    }
}
