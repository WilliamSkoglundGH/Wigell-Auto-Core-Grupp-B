package com.wac.autocore.service.discount;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class PercentageDiscount implements DiscountStrategy{
    private final BigDecimal percentage;

    public PercentageDiscount(BigDecimal percentage){
        this.percentage = percentage;
    }


    @Override
    public BigDecimal calculateDiscount(BigDecimal amount) {
        if (amount == null || percentage == null) {
            return BigDecimal.ZERO;
        }

        // 1. Multiplicera med procenten
        // 2. Dividera med 100 (som BigDecimal) och ange avrundning (t.ex. 2 decimaler, HALF_UP)
        return amount.multiply(percentage)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }
}
