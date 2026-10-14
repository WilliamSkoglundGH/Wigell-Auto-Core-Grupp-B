package com.wac.autocore.service;

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
import org.springframework.dao.DataAccessException;
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

    public InvoiceService(InvoiceRepository invoiceRepository,
                          WorkOrderRepository workOrderRepository,
                          DiscountFactory discountFactory) {
        this.invoiceRepository = invoiceRepository;
        this.workOrderRepository = workOrderRepository;
        this.discountFactory = discountFactory;
    }

    @Transactional(readOnly = true)
    public List<InvoiceSummaryDto> getAllInvoices() {
        List<Invoice> invoices = invoiceRepository.findAll();
        List<InvoiceSummaryDto> dtoList = new ArrayList<>();

        for (Invoice inv : invoices) {
            dtoList.add(InvoiceMapper.toInvoiceSummaryDto(inv));
        }

        return dtoList;
    }

    @Transactional(readOnly = true)
    public InvoiceDetailsDto getInvoice(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new InvoiceNotFoundException("invoice.error.not_found"));

        return InvoiceMapper.toInvoiceDetailsDto(invoice);
    }

    @Transactional
    public InvoiceSummaryDto createInvoice(InvoiceCreateDto dto) {

        Long workOrderId = dto.getWorkOrderId();
        String discountCode = dto.getDiscountCode();

        WorkOrder workOrder = workOrderRepository.findById(workOrderId)
                .orElseThrow(() -> new WorkOrderNotFoundException("invoice.error.workorder_not_found"));

        if (!WorkOrderState.COMPLETED.equals(workOrder.getStatus())) {
            throw new WorkOrderNotReadyForInvoiceException("invoice.error.workorder_not_completed");
        }

        if (invoiceRepository.existsByWorkOrderId(workOrderId)) {
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
            throw new InvoiceCreateException("invoice.error.create_invoice");
        }

        return InvoiceMapper.toInvoiceSummaryDto(invoice);
    }
}




