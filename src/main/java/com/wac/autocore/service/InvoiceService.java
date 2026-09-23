
package com.wac.autocore.service;

import com.wac.autocore.service.discount.DiscountStrategy;
import com.wac.autocore.service.discount.FixedDiscount;
import com.wac.autocore.service.discount.PercentageDiscount;
import com.wac.autocore.exception.InvoiceNotFoundException;
import com.wac.autocore.exception.WorkOrderNotFoundException;
import com.wac.autocore.model.Customer;
import com.wac.autocore.model.Invoice;
import com.wac.autocore.model.ServiceItem;
import com.wac.autocore.model.WorkOrder;
import com.wac.autocore.repository.InvoiceRepository;
import com.wac.autocore.repository.WorkOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class InvoiceService {
    private final InvoiceRepository invoiceRepository;
    private final WorkOrderRepository workOrderRepository;

    public InvoiceService(InvoiceRepository invoiceRepository, WorkOrderRepository workOrderRepository) {
        this.invoiceRepository = invoiceRepository;
        this.workOrderRepository = workOrderRepository;
    }

    @Transactional(readOnly = true)
    public List<Invoice> getAllInvoices() {
        return invoiceRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Invoice getInvoice(Long invoiceId) {
        return invoiceRepository.findById(invoiceId).orElseThrow(() -> new InvoiceNotFoundException(
                "Invoice with ID: " + invoiceId + " not found"
        ));
    }

    @Transactional
    public Invoice createInvoice(Long workOrderId, String discountCode) {
        WorkOrder selectedWorkOrder = workOrderRepository.findById(workOrderId).orElseThrow(() -> new WorkOrderNotFoundException(
                "WorkOrder with ID: " + workOrderId + " not found"
        ));
        if (!selectedWorkOrder.getStatus().equals("COMPLETED")) {
            throw new IllegalStateException(
                    "Invoice can only be created for a completed work order"
            );
        }

        if (invoiceRepository.existsByWorkOrderId(workOrderId)) {
            throw new IllegalStateException(
                    "An invoice already exists for this work order"
            );
        }

        double workOrderPrice = 0.0;
        for (ServiceItem serviceItem : selectedWorkOrder.getServiceItems()) {
            workOrderPrice += serviceItem.getPrice();
        }

        double discount = 0.0;
        Customer customer = selectedWorkOrder.getBooking().getVehicle().getCustomer();

        if (customer.isVip()) {
            DiscountStrategy vipDiscount = new PercentageDiscount(10);
            discount += vipDiscount.calculateDiscount(workOrderPrice);
        }

        if (discountCode != null && !discountCode.trim().isEmpty()) {
            DiscountStrategy discountCodeStrategy = null;

            if (discountCode.equalsIgnoreCase("WELCOME10")) {
                discountCodeStrategy = new PercentageDiscount(10);
            } else if (discountCode.equalsIgnoreCase("SERVICE200")) {
                discountCodeStrategy = new FixedDiscount(200);
            }

            if (discountCodeStrategy != null) {
                discount += discountCodeStrategy.calculateDiscount(workOrderPrice);
            }
        }

            if (discount > workOrderPrice) {
            discount = workOrderPrice;
            }

            Invoice invoice = new Invoice(selectedWorkOrder, LocalDate.now(),
                    workOrderPrice, discount);

            return invoiceRepository.save(invoice);
    }

}


