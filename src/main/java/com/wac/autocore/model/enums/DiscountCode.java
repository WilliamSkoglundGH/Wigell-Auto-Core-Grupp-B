package com.wac.autocore.model.enums;

import java.math.BigDecimal;

public enum DiscountCode {
    WELCOME10(DiscountType.PERCENTAGE, BigDecimal.valueOf(10)),
    SERVICE200(DiscountType.FIXED, BigDecimal.valueOf(200));

    private final DiscountType type;
    private final BigDecimal value;

    DiscountCode(DiscountType type, BigDecimal value) {
        this.type = type;
        this.value = value;
    }

    public DiscountType getType() {
        return type;
    }

    public BigDecimal getValue() {
        return value;
    }
}