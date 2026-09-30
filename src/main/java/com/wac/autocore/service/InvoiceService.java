
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

import java.math.BigDecimal;
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
                    "invoice.error.workorder_not_completed"
            );
        }

        if (invoiceRepository.existsByWorkOrderId(workOrderId)) {
            throw new IllegalStateException(
                    "invoice.error.already_exists"
            );
        }

        BigDecimal workOrderPrice = BigDecimal.ZERO;
        //TODO glöm inte att byta tillbaka här!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        /*for (ServiceItem serviceItem : selectedWorkOrder.getServiceItems()) {
            workOrderPrice += serviceItem.getPrice();
        }*/


        BigDecimal discount = BigDecimal.ZERO;
        Customer customer = selectedWorkOrder.getBooking().getVehicle().getCustomer();

        if (customer.isVip()) {
            DiscountStrategy vipDiscount = new PercentageDiscount(BigDecimal.valueOf(10));
            discount = discount.add(vipDiscount.calculateDiscount(workOrderPrice));
        }

        if (discountCode != null && !discountCode.trim().isEmpty()) {
            DiscountStrategy discountCodeStrategy = null;

            if (discountCode.equalsIgnoreCase("WELCOME10")) {
                discountCodeStrategy = new PercentageDiscount(BigDecimal.valueOf(10));
            } else if (discountCode.equalsIgnoreCase("SERVICE200")) {
                discountCodeStrategy = new FixedDiscount(BigDecimal.valueOf(200));
            }

            if (discountCodeStrategy != null) {
                discount = discount.add(discountCodeStrategy.calculateDiscount(workOrderPrice));
            }
        }
        if (discount.compareTo(workOrderPrice) > 0) {
            // Gör någonting, t.ex. sätt rabatten till att maximalt vara lika med priset
            discount = workOrderPrice;
        }


            Invoice invoice = new Invoice(selectedWorkOrder, LocalDate.now(),
                    workOrderPrice, discount);

            return invoiceRepository.save(invoice);
    }

}


