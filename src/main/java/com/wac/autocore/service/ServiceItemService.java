package com.wac.autocore.service;

import com.wac.autocore.exception.ServiceItemNotFoundException;
import com.wac.autocore.model.ServiceItem;
import com.wac.autocore.model.ServiceItemPrice;
import com.wac.autocore.repository.ServiceItemRepository;
import com.wac.autocore.repository.ServiceItemPriceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class ServiceItemService {

    private final ServiceItemRepository serviceItemRepository;
    private final ServiceItemPriceRepository priceRepository;

    public ServiceItemService(ServiceItemRepository serviceItemRepository,
                              ServiceItemPriceRepository priceRepository) {
        this.serviceItemRepository = serviceItemRepository;
        this.priceRepository = priceRepository;
    }

    @Transactional(readOnly = true)
    public List<ServiceItem> getAllServiceItems() {
        return serviceItemRepository.findAll();
    }

    @Transactional(readOnly = true)
    public ServiceItem getServiceItem(Long serviceItemId) {
        return serviceItemRepository.findById(serviceItemId)
                .orElseThrow(() -> new ServiceItemNotFoundException(
                        "Service item with ID: " + serviceItemId + " not found"));
    }

    // Ändra pris direkt på table
    @Transactional
    public void changePrice(Long serviceItemId, double newPrice) {

        ServiceItem item = serviceItemRepository.findById(serviceItemId)
                .orElseThrow(() -> new ServiceItemNotFoundException(
                        "Service item with ID: " + serviceItemId + " not found"));

        ServiceItemPrice current = priceRepository.findCurrentPrice(serviceItemId);

        if (current != null) {
            // Stäng den aktiva versionen
            current.setValidTo(LocalDate.now().minusDays(1));
            priceRepository.save(current);
        }

        // 3. Skapa ny prisversion
        ServiceItemPrice newVersion = new ServiceItemPrice(
                item,
                BigDecimal.valueOf(newPrice),
                LocalDate.now()
        );
        priceRepository.save(newVersion);

        // 4. Uppdatera ServiceItem.price (för UI)
        item.setPrice(newPrice);
        serviceItemRepository.save(item);
    }
}
