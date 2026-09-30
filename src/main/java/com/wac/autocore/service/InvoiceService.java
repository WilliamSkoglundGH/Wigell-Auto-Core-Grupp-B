
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
        // Går bara att ha Service 200 kr på totalen - hur räknar man då??


        BigDecimal workOrderTotalPrice = BigDecimal.ZERO; //STORA TOTALEN
        BigDecimal discount = BigDecimal.ZERO; // Stora i kronor

        List<InvoiceLine> invoiceLines = new ArrayList<>();

        Customer customer = selectedWorkOrder.getBooking().getVehicle().getCustomer();

        if(isDiscount(customer, discountCode)) {


            if (discountCode != null && !discountCode.trim().isEmpty()) {
            DiscountStrategy discountCodeStrategy = null;

            if (discountCode.equalsIgnoreCase("WELCOME10")) {
                discountCodeStrategy = new PercentageDiscount(10);
            }
            /*else if (discountCode.equalsIgnoreCase("SERVICE200")) {
                discountCodeStrategy = new FixedDiscount(200);
            }*/
        }
            for (WorkOrderServiceItem serviceItem : selectedWorkOrder.getServiceItems()) {
                BigDecimal amount = serviceItem.getPriceAtTime();
                BigDecimal lineDiscount = BigDecimal.ZERO;
                if(customer.isVip()){
                    DiscountStrategy vipDiscount = new PercentageDiscount(10);
                    lineDiscount = vipDiscount.calculateDiscount(amount);
                }
                if (discountCode != null && !discountCode.trim().isEmpty()) {
                    DiscountStrategy discountCodeStrategy = null;
                    if (discountCode.equalsIgnoreCase("WELCOME10")) {
                        discountCodeStrategy = new PercentageDiscount(10);
                        lineDiscount = discountCodeStrategy.calculateDiscount(amount);
                    }}
                if (lineDiscount.compareTo(workOrderTotalPrice) > 0) {
                    lineDiscount = workOrderTotalPrice;
                }

            InvoiceLine line = createInvoiceLine(serviceItem.getServiceItem().getName(),amount,lineDiscount);
            invoiceLines.add(line);

            // Uppdaterar stora summorna
            workOrderTotalPrice = workOrderTotalPrice.add(serviceItem.getPriceAtTime());
            discount = discount.add(lineDiscount);
                if (discount.compareTo(workOrderTotalPrice) > 0) {
                    discount = workOrderTotalPrice;
                }
            }

             // skriv om med metoder
         /*   if (discountCodeStrategy != null) {
                discount += discountCodeStrategy.calculateDiscount(workOrderTotalPrice.doubleValue());
            }*/
        }


            //Skapa invoicen
            Invoice invoice = new Invoice(selectedWorkOrder, LocalDate.now(),
                    workOrderTotalPrice.doubleValue(), discount.doubleValue(),invoiceLines);

            return invoiceRepository.save(invoice);
    }

    // Lägg totala discount och summan?? utanför.

    // lägg en loopish där du checkar av för varje line om kund är vip och discountcode.
    // Lägg till i totala discounten summan samt radens på line.
/*
    public BigDecimal countTotal(WorkOrder selectedWorkOrder,Customer customer) {
        BigDecimal total = BigDecimal.ZERO; //STORA TOTALEN
        List<InvoiceLine> lines = new ArrayList<>();
        for (WorkOrderServiceItem serviceItem : selectedWorkOrder.getServiceItems()) {


            lines.add(line);
            total = total.add(serviceItem.getPriceAtTime()); }

        return total;
    }
    /*
 */
    public double checkVipSetDiscount(Customer customer, BigDecimal total) {
     if (customer.isVip()) {
         DiscountStrategy vipDiscount = new PercentageDiscount(10);
         return vipDiscount.calculateDiscount(total.doubleValue());
     } else {return 0.0;}
 }



    public boolean isDiscount(Customer customer, String discountCode){
        if(customer.isVip()|| discountCode != null && !discountCode.trim().isEmpty()){ return true;}
        else {return false;}
    }



    public InvoiceLine createInvoiceLine(String name, BigDecimal amount, BigDecimal discount) {
        return new InvoiceLine(name,amount.doubleValue(),discount.doubleValue());
         }





}


