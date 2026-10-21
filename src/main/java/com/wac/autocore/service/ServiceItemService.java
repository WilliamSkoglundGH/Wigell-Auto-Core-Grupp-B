package com.wac.autocore.service;

import com.wac.autocore.dto.serviceItem.ServiceItemSummaryDto;
import com.wac.autocore.exception.EntityNotFoundException;
import com.wac.autocore.mapper.ServiceItemMapper;
import com.wac.autocore.model.ServiceItem;
import com.wac.autocore.repository.ServiceItemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ServiceItemService {

    private static final Logger logger = LoggerFactory.getLogger(ServiceItemService.class);

    private final ServiceItemRepository serviceItemRepository;

    public ServiceItemService(ServiceItemRepository serviceItemRepository) {
        this.serviceItemRepository = serviceItemRepository;
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public List<ServiceItemSummaryDto> getAllServiceItems() {
        return serviceItemRepository.findAll().stream()
                .map(ServiceItemMapper::toServiceItemDto)
                .collect(Collectors.toList());
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public ServiceItemSummaryDto getServiceItem(Long serviceItemId) {
        ServiceItem item = serviceItemRepository.findById(serviceItemId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Service item with ID: " + serviceItemId + " not found"));
        return ServiceItemMapper.toServiceItemDto(item);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void changePrice(Long serviceItemId, BigDecimal newPrice) {

        ServiceItem item = serviceItemRepository.findById(serviceItemId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Service item with ID: " + serviceItemId + " not found"));

        item.setPrice(newPrice);
        serviceItemRepository.save(item);

    }

    //Hjälpmetoder inom servicelagret

    protected List<ServiceItem> getAllServiceItemsEntity(List<Long> ids) {
        return serviceItemRepository.findAllById(ids);
    }

    protected ServiceItem getServiceItemEntity(Long serviceItemId) {
        return serviceItemRepository.findById(serviceItemId)
                .orElseThrow(() -> new EntityNotFoundException("serviceItem.error.could_not_load"));
    }


}

