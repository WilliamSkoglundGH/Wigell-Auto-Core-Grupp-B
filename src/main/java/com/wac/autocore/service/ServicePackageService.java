package com.wac.autocore.service;

import com.wac.autocore.model.ServiceItem;
import com.wac.autocore.model.ServicePackage;
import com.wac.autocore.repository.ServiceItemRepository;
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

}
