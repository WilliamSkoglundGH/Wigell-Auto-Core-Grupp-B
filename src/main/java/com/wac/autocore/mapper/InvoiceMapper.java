package com.wac.autocore.mapper;

import com.wac.autocore.dto.invoiceLine.InvoiceDto;
import com.wac.autocore.dto.invoiceLine.InvoiceLineDto;
import com.wac.autocore.model.Invoice;
import com.wac.autocore.model.InvoiceLine;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public class InvoiceMapper {


    public static InvoiceDto toInvoiceDto(Invoice invoice) {
        List<InvoiceLineDto> lines = invoice.getInvoiceLines().stream()
                .map(InvoiceMapper::toLineDto)
                .collect(Collectors.toList());

        return new InvoiceDto(
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
        BigDecimal total = line.getAmount().subtract(line.getDiscount());
        return new InvoiceLineDto(
                line.getId(),
                line.getNameOfService(),
                line.getAmount(),
                line.getDiscount(),
                total
        );
    }
}
