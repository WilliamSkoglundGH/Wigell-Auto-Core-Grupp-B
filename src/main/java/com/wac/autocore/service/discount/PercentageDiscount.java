package com.wac.autocore.service.discount;

public class PercentageDiscount implements DiscountStrategy{
    private final double percentage;

    public PercentageDiscount(double percentage){
        this.percentage = percentage;
    }


    @Override
    public double calculateDiscount(double amount) {
        return amount * percentage / 100.0;
    }
}
