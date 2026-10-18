package com.wac.autocore.mapper;

import com.wac.autocore.dto.payment.PaymentSummaryDto;
import com.wac.autocore.model.Payment;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class PaymentMapper {

    private PaymentMapper() {
    }

    public static PaymentSummaryDto toSummaryDto(Payment payment) {
        if (payment == null) {
            return null;
        }

        // Skydda mot eventuella lazy loading-problem om fakturan inte är hämtad
        Long invoiceId = (payment.getInvoice() != null) ? payment.getInvoice().getId() : null;

        return new PaymentSummaryDto(
                payment.getId(),
                invoiceId,
                payment.getAmount(),
                payment.getPaymentType(),
                payment.getPaymentDate(),
                payment.isSuccessful()
        );
    }

    public static List<PaymentSummaryDto> toSummaryDtoList(List<Payment> payments) {
        if (payments == null || payments.isEmpty()) {
            return Collections.emptyList();
        }

        return payments.stream()
                .map(PaymentMapper::toSummaryDto)
                .collect(Collectors.toList());
    }
}