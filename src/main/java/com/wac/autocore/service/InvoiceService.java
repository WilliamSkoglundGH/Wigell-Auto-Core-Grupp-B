package com.wac.autocore.service;

import com.wac.autocore.controller.javafx.InvoiceController;
import com.wac.autocore.dto.invoice.InvoiceSummaryDto;
import com.wac.autocore.dto.invoice.InvoiceDetailsDto;
import com.wac.autocore.dto.invoice.InvoiceCreateDto;
import com.wac.autocore.exception.*;
import com.wac.autocore.mapper.InvoiceMapper;
import com.wac.autocore.model.*;
import com.wac.autocore.model.enums.WorkOrderState;
import com.wac.autocore.repository.InvoiceRepository;
import com.wac.autocore.repository.WorkOrderRepository;
import com.wac.autocore.service.discount.DiscountFactory;
import com.wac.autocore.service.discount.DiscountStrategy;
import com.wac.autocore.service.discount.PercentageDiscount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final WorkOrderRepository workOrderRepository;
    private final DiscountFactory discountFactory;

    private static final Logger logger = LoggerFactory.getLogger(InvoiceService.class);

    public InvoiceService(InvoiceRepository invoiceRepository,
                          WorkOrderRepository workOrderRepository,
                          DiscountFactory discountFactory) {
        this.invoiceRepository = invoiceRepository;
        this.workOrderRepository = workOrderRepository;
        this.discountFactory = discountFactory;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public List<InvoiceSummaryDto> getAllInvoices() {
        try {
            List<Invoice> invoices = invoiceRepository.findAll();
            List<InvoiceSummaryDto> dtoList = new ArrayList<>();

            for (Invoice inv : invoices) {
                dtoList.add(InvoiceMapper.toInvoiceSummaryDto(inv));
            }

            return dtoList;
        } catch (DataAccessException e) {
            logger.error("Method: getAllInvoices | Database error ", e);
            throw new DatabaseException("common.error.database");
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public InvoiceDetailsDto getInvoice(Long invoiceId) {
        try {
            Invoice invoice = invoiceRepository.findById(invoiceId)
                    .orElseThrow(() ->
                    {
                        logger.error("Method: getInvoice | No invoice found | Id: {} ", invoiceId);
                        return new EntityNotFoundException("invoice.error.not_found");
                    });

            return InvoiceMapper.toInvoiceDetailsDto(invoice);
        } catch (DataAccessException e) {
            logger.error("Method: getInvoice | Database error ", e);
            throw new DatabaseException("common.error.database");
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public InvoiceSummaryDto createInvoice(InvoiceCreateDto dto) {

        Long workOrderId = dto.getWorkOrderId();
        String discountCode = dto.getDiscountCode();

        WorkOrder workOrder = workOrderRepository.findById(workOrderId)
                .orElseThrow(() -> {
                    logger.error("Method: createInvoice | No workorder found | Id: {} ", workOrderId);
                    return new EntityNotFoundException("invoice.error.workorder_not_found");
                });

        if (!WorkOrderState.COMPLETED.equals(workOrder.getStatus())) {
            logger.error("Method: createInvoice | Workorder not completed | Id: {} ", workOrderId);
            throw new WorkOrderNotReadyForInvoiceException("invoice.error.workorder_not_completed");
        }

        if (invoiceRepository.existsByWorkOrderId(workOrderId)) {
            logger.error("Method: createInvoice | Invoice already exists for workorder | Id: {} ", workOrderId);
            throw new InvoiceAlreadyExistsException("invoice.error.already_exists");
        }

        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal totalDiscount = BigDecimal.ZERO;
        List<InvoiceLine> invoiceLines = new ArrayList<>();

        Customer customer = workOrder.getVehicle().getCustomer();
        DiscountStrategy promoStrategy = discountFactory.createStrategy(discountCode);

        for (WorkOrderServiceItem item : workOrder.getServiceItems()) {

            BigDecimal amount = item.getPriceAtTime();
            BigDecimal discount = BigDecimal.ZERO;

            if (customer.isVip()) {
                DiscountStrategy vip = new PercentageDiscount(BigDecimal.valueOf(10));
                discount = discount.add(vip.calculateDiscount(amount));
            }

            if (promoStrategy != null) {
                discount = discount.add(promoStrategy.calculateDiscount(amount));
            }

            if (discount.compareTo(amount) > 0) {
                discount = amount;
            }

            InvoiceLine line = new InvoiceLine(item.getServiceItem().getName(), amount, discount);
            invoiceLines.add(line);

            totalAmount = totalAmount.add(amount);
            totalDiscount = totalDiscount.add(discount);
        }

        if (totalDiscount.compareTo(totalAmount) > 0) {
            totalDiscount = totalAmount;
        }

        Invoice invoice = new Invoice(workOrder, LocalDate.now(), totalAmount, totalDiscount, invoiceLines);

        try {
            invoice = invoiceRepository.save(invoice);
        } catch (DataAccessException e) {
            logger.error("Method: createInvoice | Database error ", e);
            throw new DatabaseException("common.error.database");
        }
        logger.info("Method: createInvoice | Success | InvoiceId: {}", invoice.getId());
        return InvoiceMapper.toInvoiceSummaryDto(invoice);
    }
}




