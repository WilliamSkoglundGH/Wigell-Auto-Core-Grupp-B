package com.wac.autocore.mapper;

import com.wac.autocore.dto.ServicePackageSummaryDto;
import com.wac.autocore.model.ServicePackage;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ServicePackageMapper {

    public static ServicePackageSummaryDto toDto(ServicePackage servicePackage) {
        if (servicePackage == null) {
            return null;
        }

        // Slå ihop alla tjänster till en enda sträng separerade med komma (eller semikolon)
        String servicesString = servicePackage.getServiceItems().stream()
                .map(item -> item.getId() + ": " + item.getName())
                .collect(Collectors.joining(", "));

        return new ServicePackageSummaryDto(
                servicePackage.getId(),
                servicePackage.getName(),
                servicePackage.getDescription(),
                servicePackage.isActive(),
                servicesString
        );
    }

    public static List<ServicePackageSummaryDto> toDtoList(List<ServicePackage> servicePackages) {
        if (servicePackages == null) {
            return Collections.emptyList();
        }
        return servicePackages.stream()
                .map(ServicePackageMapper::toDto)
                .collect(Collectors.toList());
    }
}