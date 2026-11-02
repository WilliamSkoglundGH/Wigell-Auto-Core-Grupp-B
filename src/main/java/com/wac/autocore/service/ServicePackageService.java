package com.wac.autocore.service;

import com.wac.autocore.dto.serviceItem.ServiceItemSummaryDto;
import com.wac.autocore.dto.servicePackage.ServicePackageDetailDto;
import com.wac.autocore.dto.servicePackage.ServicePackageSummaryDto;
import com.wac.autocore.exception.DatabaseException;
import com.wac.autocore.exception.EntityNotFoundException;
import com.wac.autocore.mapper.ServicePackageMapper;
import com.wac.autocore.model.ServiceItem;
import com.wac.autocore.model.ServicePackage;
import com.wac.autocore.repository.ServicePackageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ServicePackageService {

    private final ServiceItemService serviceItemService;
    private final ServicePackageRepository servicePackageRepository;

    private static final Logger logger = LoggerFactory.getLogger(ServicePackageService.class);

    public ServicePackageService(ServicePackageRepository servicePackageRepository, ServiceItemService serviceItemService) {
        this.servicePackageRepository = servicePackageRepository;
        this.serviceItemService = serviceItemService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void savePackage(String packageName, String packageDescription, List<ServiceItemSummaryDto> selectedServices) {
        try {
            List<Long> ids = selectedServices.stream()
                    .map(ServiceItemSummaryDto::getId)
                    .collect(Collectors.toList());

            List<ServiceItem> serviceItems = serviceItemService.getAllServiceItemsEntity(ids);

            ServicePackage servicePackage = new ServicePackage(packageName, packageDescription);
            servicePackage.setServiceItems(new java.util.HashSet<>(serviceItems));

            servicePackageRepository.save(servicePackage);
            logger.info("Method: savePackage | Success | PackageId: {}", servicePackage.getId());
        } catch (DataAccessException e) {
            logger.error("Method: savePackage | Database error ", e);
            throw new DatabaseException("common.error.database");
        }
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public List<ServicePackageSummaryDto> getAllPackagesSummary() {
        try {
            List<ServicePackage> packages = servicePackageRepository.findAll();
            return ServicePackageMapper.toDtoList(packages);
        } catch (DataAccessException e) {
            logger.error("Method: getAllPackagesSummary | Database error ", e);
            throw new DatabaseException("common.error.database");
        }
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public List<ServicePackageDetailDto> getAllPackagesDetail() {
        try {
            List<ServicePackage> packages = servicePackageRepository.findAll();
            return ServicePackageMapper.toDetailDtoList(packages);
        } catch (DataAccessException e) {
            logger.error("Method: getAllPackagesDetail | Database error ", e);
            throw new DatabaseException("common.error.database");
        }
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public ServicePackageDetailDto getPackage(Long id) {
        try {
            return ServicePackageMapper.toDetailDto(servicePackageRepository.findById(id)
                    .orElseThrow(() -> {
                        logger.error("Method: getPackage | No service package found | Id: {} ", id);
                        return new EntityNotFoundException("serviceItem.package.error.not_found");
                    }));
        } catch (DataAccessException e) {
            logger.error("Method: getPackage | Database error ", e);
            throw new DatabaseException("common.error.database");
        }
    }
}
