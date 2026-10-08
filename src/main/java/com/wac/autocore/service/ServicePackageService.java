package com.wac.autocore.service;

import com.wac.autocore.dto.servicePackage.ServicePackageDetailDto;
import com.wac.autocore.dto.servicePackage.ServicePackageSummaryDto;
import com.wac.autocore.exception.ServicePackageNotFoundException;
import com.wac.autocore.mapper.ServicePackageMapper;
import com.wac.autocore.model.ServiceItem;
import com.wac.autocore.model.ServicePackage;
import com.wac.autocore.repository.ServicePackageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
@Service
public class ServicePackageService {

    private final ServicePackageRepository servicePackageRepository;

    public ServicePackageService(ServicePackageRepository servicePackageRepository) {
        this.servicePackageRepository = servicePackageRepository;
    }

    @Transactional
    public void savePackage(String packageName, String packageDescription, List<ServiceItem> selectedServices){
        ServicePackage servicePackage = new ServicePackage(packageName, packageDescription);
        servicePackage.setServiceItems(new java.util.HashSet<>(selectedServices));
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
