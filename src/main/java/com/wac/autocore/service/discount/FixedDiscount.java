package com.wac.autocore.service.discount;

public class FixedDiscount implements DiscountStrategy{
    private final double discountAmount;

    public FixedDiscount(double discountAmount){
        this.discountAmount = discountAmount;
    }

    @Override
    public double calculateDiscount(double amount) {
        return discountAmount;
    }
}
