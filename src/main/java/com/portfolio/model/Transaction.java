
package com.portfolio.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Objects;

public final class Transaction {

    // Fields
    private final String symbol;
    private final TransactionType type;
    private final int quantity;
    private final BigDecimal price;
    private final LocalDate date;

    // Constructor
    public Transaction(
            String symbol,
            TransactionType type,
            int quantity,
            BigDecimal price,
            LocalDate date) {

        // Validate stock symbol
        if (symbol == null || symbol.isBlank()) {
            throw new IllegalArgumentException(
                "Stock symbol cannot be empty"
            );
        }

        // Validate quantity
        if (quantity <= 0) {
            throw new IllegalArgumentException(
                "Quantity must be positive"
            );
        }

        // Validate price
        if (price == null || price.signum() <= 0) {
            throw new IllegalArgumentException(
                "Price must be positive"
            );
        }

        // Initialise fields
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

    // Getters
    public String getSymbol() {
        return symbol;
    }

    public TransactionType getType() {
        return type;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public LocalDate getDate() {
        return date;
    }
}
