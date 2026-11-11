
package com.portfolio.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Objects;

public final class Transaction {

    private final String symbol;
    private final TransactionType type;
    private final BigDecimal quantity;
    private final BigDecimal price;
    private final LocalDate date;

    public Transaction(
            String symbol,
            TransactionType type,
            BigDecimal quantity,
            BigDecimal price,
            LocalDate date) {

        if (symbol == null || symbol.isBlank()) {
            throw new IllegalArgumentException(
                "Stock symbol cannot be empty"
            );
        }

        if (quantity == null || quantity.signum() <= 0) {
            throw new IllegalArgumentException(
                "Quantity must be positive"
            );
        }

        if (price == null || price.signum() <= 0) {
            throw new IllegalArgumentException(
                "Price must be positive"
            );
        }

        this.symbol = symbol.trim()
                .toUpperCase(Locale.ROOT);

        this.type = Objects.requireNonNull(
            type, "Transaction type cannot be null"
        );

        this.quantity = quantity;
        this.price = price;

        this.date = Objects.requireNonNull(
            date, "Transaction date cannot be null"
        );
    }

    public String getSymbol() {
        return symbol;
    }

    public TransactionType getType() {
        return type;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public LocalDate getDate() {
        return date;
    }
}
