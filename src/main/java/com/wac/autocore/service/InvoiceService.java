
package com.wac.autocore.service;

import com.wac.autocore.model.*;
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
import org.hibernate.jdbc.Work;
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

        BigDecimal workOrderTotalPrice = BigDecimal.ZERO; //STORA TOTALEN
        BigDecimal discount = BigDecimal.ZERO; // Stora i kronor

        List<InvoiceLine> invoiceLines = new ArrayList<>();

        Customer customer = selectedWorkOrder.getBooking().getVehicle().getCustomer();

            for (WorkOrderServiceItem serviceItem : selectedWorkOrder.getServiceItems()) {
                BigDecimal amount = serviceItem.getPriceAtTime();
                BigDecimal lineDiscount = BigDecimal.ZERO;

                if (discountCode != null && !discountCode.trim().isEmpty()) {
                    DiscountStrategy discountCodeStrategy = null;

                    /*if (discountCode.equalsIgnoreCase("SERVICE200")) {
                        discountCodeStrategy = new FixedDiscount(BigDecimal.valueOf(200));
                        //om discountCodeStrategy is instanceOf FixedDiscount gör såhär.
                        if (customer.isVip()){}
                        break; // Lämna
                    }*/

                    if (customer.isVip()) {
                        DiscountStrategy vipDiscount = new PercentageDiscount(BigDecimal.valueOf(10));
                        lineDiscount = vipDiscount.calculateDiscount(amount);
                    }
                    if (discountCode.equalsIgnoreCase("WELCOME10"))
                    // ? Lägga in godkända discountkoder i en fil kopplad till antal procent och loopa igenom denna för att sätta discount värderna.
                    { discountCodeStrategy = new PercentageDiscount(BigDecimal.valueOf(10));
                        lineDiscount = discountCodeStrategy.calculateDiscount(amount);
                    }
                }
                if (lineDiscount.compareTo(amount) > 0) {
                    lineDiscount = amount;
                }

                InvoiceLine line = new InvoiceLine(serviceItem.getServiceItem().getName(), amount, lineDiscount);
                invoiceLines.add(line);

                // Uppdaterar stora summorna
                workOrderTotalPrice = workOrderTotalPrice.add(serviceItem.getPriceAtTime());
                discount = discount.add(lineDiscount);
                if (discount.compareTo(workOrderTotalPrice) > 0) {
                    discount = workOrderTotalPrice;
                }
            }

            //Skapa invoicen
            Invoice invoice = new Invoice(selectedWorkOrder, LocalDate.now(),
                    workOrderTotalPrice, discount,invoiceLines);

            return invoiceRepository.save(invoice);
    }


}


