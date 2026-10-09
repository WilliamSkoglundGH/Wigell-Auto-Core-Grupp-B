package com.wac.autocore.service.discount;

import java.math.BigDecimal;

public class FixedDiscount implements DiscountStrategy{
    private final BigDecimal discountAmount;

    public FixedDiscount(BigDecimal discountAmount){
        this.discountAmount = discountAmount;
    }

    @Override
    public BigDecimal calculateDiscount(BigDecimal amount) {
        return discountAmount;
    }
}
