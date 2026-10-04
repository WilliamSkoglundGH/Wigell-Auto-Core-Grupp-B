
package com.wac.autocore.service;

import com.wac.autocore.dto.invoiceLine.InvoiceDto;
import com.wac.autocore.mapper.InvoiceMapper;
import com.wac.autocore.model.*;
import com.wac.autocore.service.discount.DiscountFactory;
import com.wac.autocore.service.discount.DiscountStrategy;
import com.wac.autocore.service.discount.PercentageDiscount;
import com.wac.autocore.exception.InvoiceNotFoundException;
import com.wac.autocore.exception.WorkOrderNotFoundException;
import com.wac.autocore.model.Customer;
import com.wac.autocore.model.Invoice;
import com.wac.autocore.model.WorkOrder;
import com.wac.autocore.repository.InvoiceRepository;
import com.wac.autocore.repository.WorkOrderRepository;
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
    private final DiscountFactory discountFactory; // <-- Injicerar fabriken här

    public InvoiceService(InvoiceRepository invoiceRepository,
                          WorkOrderRepository workOrderRepository,
                          DiscountFactory discountFactory) {
        this.invoiceRepository = invoiceRepository;
        this.workOrderRepository = workOrderRepository;
        this.discountFactory = discountFactory;
    }
    @Transactional(readOnly = true)
    public List<Invoice> getAllInvoices() {
        return invoiceRepository.findAll();
    }

    @Transactional(readOnly = true)
    public InvoiceDto getInvoice(Long invoiceId) {
         Invoice invoice = invoiceRepository.findById(invoiceId).orElseThrow(() -> new InvoiceNotFoundException(
                "Invoice with ID: " + invoiceId + " not found"
        ));
        return InvoiceMapper.toInvoiceDto(invoice);
    }

    @Transactional
    public Invoice createInvoice(Long workOrderId, String discountCode) {
        WorkOrder selectedWorkOrder = workOrderRepository.findById(workOrderId).orElseThrow(() -> new WorkOrderNotFoundException(
                "WorkOrder with ID: " + workOrderId + " not found"
        ));

        if (!WorkOrderState.COMPLETED.equals(selectedWorkOrder.getStatus())) {
            throw new IllegalStateException("invoice.error.workorder_not_completed");
        }

        if (invoiceRepository.existsByWorkOrderId(workOrderId)) {
            throw new IllegalStateException("invoice.error.already_exists");
        }

        BigDecimal workOrderTotalPrice = BigDecimal.ZERO;
        BigDecimal discount = BigDecimal.ZERO;
        List<InvoiceLine> invoiceLines = new ArrayList<>();

        Customer customer = selectedWorkOrder.getBooking().getVehicle().getCustomer();

        // Hämta kampanjstrategi via fabriken (som nu kollar Enumet)
        DiscountStrategy promoStrategy = discountFactory.createStrategy(discountCode);

        for (WorkOrderServiceItem serviceItem : selectedWorkOrder.getServiceItems()) {
            BigDecimal amount = serviceItem.getPriceAtTime();
            BigDecimal lineDiscount = BigDecimal.ZERO;

            if (customer.isVip()) {
                DiscountStrategy vipDiscount = new PercentageDiscount(BigDecimal.valueOf(10));
                lineDiscount = lineDiscount.add(vipDiscount.calculateDiscount(amount));
            }

            if (promoStrategy != null) {
                lineDiscount = lineDiscount.add(promoStrategy.calculateDiscount(amount));
            }

            if (lineDiscount.compareTo(amount) > 0) {
                lineDiscount = amount;
            }

            InvoiceLine line = new InvoiceLine(serviceItem.getServiceItem().getName(), amount, lineDiscount);
            invoiceLines.add(line);

            workOrderTotalPrice = workOrderTotalPrice.add(amount);
            discount = discount.add(lineDiscount);
        }

        if (discount.compareTo(workOrderTotalPrice) > 0) {
            discount = workOrderTotalPrice;
        }

        Invoice invoice = new Invoice(selectedWorkOrder, LocalDate.now(),
                workOrderTotalPrice, discount, invoiceLines);

        return invoiceRepository.save(invoice);
    }
}


