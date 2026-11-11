
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

    this.symbol = symbol;
    this.type = type;
    this.quantity = quantity;
    this.price = price;
    this.date = date;
}


}
