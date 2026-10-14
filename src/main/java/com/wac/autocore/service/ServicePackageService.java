package com.wac.autocore.service;

import com.wac.autocore.dto.serviceItem.ServiceItemCreateDto;
import com.wac.autocore.dto.serviceItem.ServiceItemSummaryDto;
import com.wac.autocore.dto.servicePackage.ServicePackageDetailDto;
import com.wac.autocore.dto.servicePackage.ServicePackageSummaryDto;
import com.wac.autocore.exception.ServicePackageNotFoundException;
import com.wac.autocore.mapper.ServiceItemMapper;
import com.wac.autocore.mapper.ServicePackageMapper;
import com.wac.autocore.model.ServiceItem;
import com.wac.autocore.model.ServicePackage;
import com.wac.autocore.repository.ServiceItemRepository;
import com.wac.autocore.repository.ServicePackageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ServicePackageService {

    private final ServiceItemRepository serviceItemRepository;
    private final ServicePackageRepository servicePackageRepository;

    public ServicePackageService(ServicePackageRepository servicePackageRepository, ServiceItemRepository serviceItemRepository) {
        this.servicePackageRepository = servicePackageRepository;
        this.serviceItemRepository = serviceItemRepository;
    }

    @Transactional
    public void savePackage(String packageName, String packageDescription, List<ServiceItemSummaryDto> selectedServices){

        List<Long> ids = selectedServices.stream()
                .map(ServiceItemSummaryDto::getId)
                .collect(Collectors.toList());
        //TODO Lägga en check för att kolla så att det är rätt items.
        List<ServiceItem> serviceItems = serviceItemRepository.findAllById(ids);

        ServicePackage servicePackage = new ServicePackage(packageName, packageDescription);
        servicePackage.setServiceItems(new java.util.HashSet<>(serviceItems));

        servicePackageRepository.save(servicePackage);
    }

    @Transactional(readOnly = true)
    public List<ServicePackageSummaryDto> getAllPackagesSummary() {
        List<ServicePackage> packages = servicePackageRepository.findAll();
        return ServicePackageMapper.toDtoList(packages);
    }

    @Transactional(readOnly = true)
    public List<ServicePackageDetailDto> getAllPackagesDetail() {
        List<ServicePackage> packages = servicePackageRepository.findAll();
        return ServicePackageMapper.toDetailDtoList(packages);
    }

    @Transactional(readOnly = true)
    public ServicePackageDetailDto getPackage(Long id) {
        return ServicePackageMapper.toDetailDto( servicePackageRepository.findById(id)
                .orElseThrow(() -> new ServicePackageNotFoundException("service_package.error.not_found")));
    }
}
