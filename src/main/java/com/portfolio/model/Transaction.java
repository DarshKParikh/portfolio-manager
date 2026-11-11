
package com.portfolio.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Transaction {

    private String symbol;
    private TransactionType type;
    private int quantity;
    private BigDecimal price;
    private LocalDate date;


public Transaction(
        String symbol,
        TransactionType type,
        int quantity,
        BigDecimal price,
        LocalDate date) {

    if (quantity <= 0) {
        throw new IllegalArgumentException(
            "Quantity must be positive"
        );
    }


    if (price == null || price.signum() <= 0) {
        throw new IllegalArgumentException(
            "Price must be positive"
        );
    }


    if (symbol == null || symbol.isBlank()) {
        throw new IllegalArgumentException(
            "Stock symbol cannot be empty"
        );
    }



        
    this.symbol = symbol.trim().toUpperCase(
        java.util.Locale.ROOT);
    this.type = type;
    this.quantity = quantity;
    this.price = price;
    this.date = date;
}


}
