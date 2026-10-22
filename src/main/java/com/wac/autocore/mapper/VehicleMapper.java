package com.wac.autocore.mapper;

import com.wac.autocore.dto.vechicle.VehicleCreateDto;
import com.wac.autocore.dto.vechicle.VehicleSummaryDto;
import com.wac.autocore.model.Customer;
import com.wac.autocore.model.Vehicle;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class VehicleMapper {

    private VehicleMapper() {
    }

    public static VehicleSummaryDto toDto(Vehicle vehicle) {
        if (vehicle == null) {
            return null;
        }
        String customerName = (vehicle.getCustomer() != null) ? vehicle.getCustomer().getName() : null;
        return new VehicleSummaryDto(
                vehicle.getId(),
                vehicle.getRegistrationNumber(),
                vehicle.getBrand(),
                vehicle.getModel(),
                vehicle.getYear(),
                customerName
        );
    }

    public static List<VehicleSummaryDto> toDtoList(List<Vehicle> vehicles) {
        if (vehicles == null || vehicles.isEmpty()) {
            return Collections.emptyList();
        }

        return vehicles.stream()
                .map(VehicleMapper::toDto)
                .collect(Collectors.toList());
    }
    public static Vehicle toEntity(VehicleCreateDto dto, Customer customer) {
        if (dto == null) {
            return null;
        }

        return new Vehicle(
                dto.getRegistrationNumber(),
                dto.getBrand(),
                dto.getModel(),
                dto.getYear(),
                customer
        );
    }
}