package com.wac.autocore.gui.util;

import javafx.util.StringConverter;

import java.math.BigDecimal;

public class BigDecimalStringConverter extends StringConverter<BigDecimal> {

    @Override
    public String toString(BigDecimal value) {
        return value != null ? value.toString() : "";
    }

    @Override
    public BigDecimal fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(value.trim());
    }
}