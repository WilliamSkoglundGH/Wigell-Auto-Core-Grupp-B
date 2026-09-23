package com.wac.autocore.service;

import com.wac.autocore.exception.ServiceItemNotFoundException;
import com.wac.autocore.model.ServiceItem;
import com.wac.autocore.repository.ServiceItemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ServiceItemService {
    private static final Logger logger = LoggerFactory.getLogger(ServiceItemService.class);
    private final ServiceItemRepository serviceItemRepository;

    public ServiceItemService(ServiceItemRepository serviceItemRepository){
        this.serviceItemRepository = serviceItemRepository;
    }

    @Transactional(readOnly = true)
    public List<ServiceItem> getServiceItems(){
        return serviceItemRepository.findAll();
    }

    @Transactional(readOnly = true)
    public ServiceItem getServiceItem(Long serviceItemId) {
        return serviceItemRepository.findById(serviceItemId).orElseThrow(() -> new ServiceItemNotFoundException(
                "Service item with ID: " + serviceItemId + " not found"));

    }


}