package com.wac.autocore.service.discount;

import com.wac.autocore.model.enums.DiscountCode;
import com.wac.autocore.model.enums.DiscountType;
import org.springframework.stereotype.Component;

@Component
public class DiscountFactory {

    public DiscountStrategy createStrategy(String code) {
        if (code == null || code.trim().isEmpty()) {
            return null;
        }

        try {
            // Försök matcha strängen mot en konstant i Enumet (skiftlägeskänsligt, kollar UPPERCASE)
            DiscountCode discountCode = DiscountCode.valueOf(code.toUpperCase());

            // Bygg rätt strategi baserat på Enuets värden
            if (discountCode.getType() == DiscountType.FIXED) {
                return new FixedDiscount(discountCode.getValue());
            } else if (discountCode.getType() == DiscountType.PERCENTAGE) {
                return new PercentageDiscount(discountCode.getValue());
            }

        } catch (IllegalArgumentException e) {
            // Koden finns inte med i Enumet
            throw new IllegalArgumentException("invoice.error.invalid_discount_code");
        }

        return null;
    }
}