package com.portfolio.model;

import java.math.BigDecimal;
import java.util.Objects;

public final class Stock implements Asset {
    private final String symbol;
    private final BigDecimal price;

    public Stock(String symbol, BigDecimal price) {
        if (symbol == null || symbol.isBlank()) throw new IllegalArgumentException("Symbol is required");
        if (price == null || price.signum() < 0) throw new IllegalArgumentException("Price cannot be negative");
        this.symbol = symbol.trim().toUpperCase(java.util.Locale.ROOT);
        this.price = price;
    }
    @Override public String symbol() { return symbol; }
    @Override public BigDecimal price() { return price; }
}
