package com.wac.autocore.mapper;

import com.wac.autocore.dto.invoice.InvoiceSummaryDto;
import com.wac.autocore.dto.invoice.InvoiceDetailsDto;
import com.wac.autocore.dto.invoice.InvoiceLineDto;
import com.wac.autocore.model.Invoice;
import com.wac.autocore.model.InvoiceLine;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public final class InvoiceMapper {
     private InvoiceMapper() {}

    public static InvoiceSummaryDto toInvoiceSummaryDto(Invoice invoice) {
        return new InvoiceSummaryDto(
                invoice.getId(),
                invoice.getWorkOrder() != null ? invoice.getWorkOrder().getId() : null,
                invoice.getInvoiceDate(),
                invoice.getAmount(),
                invoice.getDiscount(),
                invoice.getTotalAmount(),
                invoice.isPaid()
        );
    }

    public static InvoiceDetailsDto toInvoiceDetailsDto(Invoice invoice) {
        List<InvoiceLineDto> lines = invoice.getInvoiceLines().stream()
                .map(InvoiceMapper::toLineDto)
                .collect(Collectors.toList());

        return new InvoiceDetailsDto(
                invoice.getId(),
                invoice.getWorkOrder() != null ? invoice.getWorkOrder().getId() : null,
                invoice.getInvoiceDate(),
                invoice.getAmount(),
                invoice.getDiscount(),
                invoice.getTotalAmount(),
                invoice.isPaid(),
                lines
        );
    }

    public static InvoiceLineDto toLineDto(InvoiceLine line) {
        BigDecimal amount = line.getAmount() != null ? line.getAmount() : BigDecimal.ZERO;
        BigDecimal discount = line.getDiscount() != null ? line.getDiscount() : BigDecimal.ZERO;

        BigDecimal totalAmount = amount.subtract(discount);
        if (totalAmount.compareTo(BigDecimal.ZERO) < 0) {
            totalAmount = BigDecimal.ZERO;
        }

        return new InvoiceLineDto(
                line.getId(),
                line.getNameOfService(),
                amount,
                discount,
                totalAmount
        );
    }
}


