package com.portfolio.model;

import java.math.BigDecimal;

public final class Bond implements Asset {
    private final String symbol;
    private final BigDecimal price;
    private final BigDecimal couponRate;

    public Bond(String symbol, BigDecimal price, BigDecimal couponRate) {
        if (symbol == null || symbol.isBlank()) throw new IllegalArgumentException("Symbol is required");
        if (price == null || price.signum() < 0) throw new IllegalArgumentException("Price cannot be negative");
        if (couponRate == null || couponRate.signum() < 0 || couponRate.compareTo(BigDecimal.ONE) > 0)
            throw new IllegalArgumentException("Coupon rate must be between 0 and 1");
        this.symbol = symbol.trim().toUpperCase(java.util.Locale.ROOT);
        this.price = price;
        this.couponRate = couponRate;
    }
    @Override public String symbol() { return symbol; }
    @Override public BigDecimal price() { return price; }
    public BigDecimal couponRate() { return couponRate; }
}
