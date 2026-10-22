package com.wac.autocore.service;

import com.wac.autocore.dto.serviceItem.ServiceItemSummaryDto;
import com.wac.autocore.exception.DatabaseException;
import com.wac.autocore.exception.EntityNotFoundException;
import com.wac.autocore.mapper.ServiceItemMapper;
import com.wac.autocore.model.ServiceItem;
import com.wac.autocore.repository.ServiceItemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
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
        try {
            return serviceItemRepository.findAll().stream()
                    .map(ServiceItemMapper::toServiceItemDto)
                    .collect(Collectors.toList());
        } catch (DataAccessException e) {
            logger.error("Method: getAllServiceItems | Database error ", e);
            throw new DatabaseException("common.error.database");
        }
        }

    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public ServiceItemSummaryDto getServiceItem(Long serviceItemId) {
        try {
            ServiceItem item = serviceItemRepository.findById(serviceItemId)
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Service item with ID: " + serviceItemId + " not found"));
            return ServiceItemMapper.toServiceItemDto(item);
        }catch (DataAccessException e) {
            logger.error("Method: getServiceItems | Database error ", e);
            throw new DatabaseException("common.error.database");
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void changePrice(Long serviceItemId, BigDecimal newPrice) {
        try {
            ServiceItem item = getServiceItemEntity(serviceItemId);
            item.setPrice(newPrice);
            serviceItemRepository.save(item);

        }catch (DataAccessException e) {
            logger.error("Method: changePrice | Database error ", e);
            throw new DatabaseException("common.error.database");
        }
    }

    //Hjälpmetoder inom servicelagret

    protected List<ServiceItem> getAllServiceItemsEntity(List<Long> ids) {
        try{
        return serviceItemRepository.findAllById(ids);
    }catch (DataAccessException e) {
        logger.error("Method: getAllServiceItemsEntity | Database error ", e);
        throw new DatabaseException("common.error.database");
    }
    }

    protected ServiceItem getServiceItemEntity(Long serviceItemId) {
        try{
        return serviceItemRepository.findById(serviceItemId)
                .orElseThrow(() ->
                { logger.error("Method: getServiceItemEntity | Service item not found | Id: {} ", serviceItemId);
                 return new EntityNotFoundException("serviceItem.error.could_not_load");
                });
        }catch (DataAccessException e) {
            logger.error("Method: getServiceItemEntity | Database error ", e);
            throw new DatabaseException("common.error.database");
        }
    }


}

