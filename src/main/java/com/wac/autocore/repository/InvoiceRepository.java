package com.wac.autocore.repository;

import com.wac.autocore.model.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    boolean existsByWorkOrderId(Long workOrderId);
}
