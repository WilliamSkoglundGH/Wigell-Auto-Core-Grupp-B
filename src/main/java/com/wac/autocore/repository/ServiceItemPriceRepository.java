package com.wac.autocore.repository;

import com.wac.autocore.model.ServiceItemPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ServiceItemPriceRepository extends JpaRepository<ServiceItemPrice, Long> {

    @Query("""
        SELECT p FROM ServiceItemPrice p
        WHERE p.serviceItem.id = :serviceItemId
          AND p.validTo IS NULL
    """)
    ServiceItemPrice findCurrentPrice(Long serviceItemId);
}


