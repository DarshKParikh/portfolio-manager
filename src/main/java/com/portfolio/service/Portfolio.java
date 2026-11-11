package com.portfolio.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.portfolio.model.Asset;
import com.portfolio.model.Transaction;
import com.portfolio.model.TransactionType;

/**
 * In-memory portfolio with transaction history.
 * Market prices are supplied explicitly.
 */
public final class Portfolio {

    // Current holdings
    private final Map<String, BigDecimal> quantities =
            new HashMap<>();

    // Asset information and latest prices
    private final Map<String, Asset> assets =
            new HashMap<>();

    // Record of successful transactions
    private final List<Transaction> transactions =
            new ArrayList<>();

    /**
     * Buy an asset and record the transaction.
     */
    public void buy(Asset asset, BigDecimal quantity) {
        validateQuantity(quantity);

        if (asset == null) {
            throw new IllegalArgumentException(
                "Asset is required"
            );
        }

        // Create transaction before modifying holdings
        Transaction transaction = new Transaction(
            asset.symbol(),
            TransactionType.BUY,
            quantity,
            asset.price(),
            LocalDate.now()
        );

        // Update holdings
        assets.put(asset.symbol(), asset);

        quantities.merge(
            asset.symbol(),
            quantity,
            BigDecimal::add
        );

        // Record successful purchase
        transactions.add(transaction);
    }

    /**
     * Sell an asset and record the transaction.
     */
    public void sell(String symbol, BigDecimal quantity) {
        validateQuantity(quantity);

        if (symbol == null || symbol.isBlank()) {
            throw new IllegalArgumentException(
                "Symbol is required"
            );
        }

        String key = symbol.trim()
                .toUpperCase(Locale.ROOT);

        BigDecimal held = quantities.getOrDefault(
            key, BigDecimal.ZERO
        );

        // Reject sales exceeding current holdings
        if (held.compareTo(quantity) < 0) {
            throw new IllegalArgumentException(
                "Insufficient holdings"
            );
        }

        // Use current stored price as execution price
        Asset asset = assets.get(key);

        Transaction transaction = new Transaction(
            key,
            TransactionType.SELL,
            quantity,
            asset.price(),
            LocalDate.now()
        );

        // Update holdings
        BigDecimal remaining = held.subtract(quantity);

        if (remaining.signum() == 0) {
            quantities.remove(key);
            assets.remove(key);
        } else {
            quantities.put(key, remaining);
        }

        // Record successful sale
        transactions.add(transaction);
    }

    /**
     * Update the market price of an existing asset.
     */
    public void updatePrice(Asset asset) {
        if (asset == null ||
                !assets.containsKey(asset.symbol())) {
            throw new IllegalArgumentException(
                "Asset must already be held"
            );
        }

        assets.put(asset.symbol(), asset);
    }

    /**
     * Calculate total portfolio market value.
     */
    public BigDecimal marketValue() {
        return quantities.entrySet()
                .stream()
                .map(entry ->
                    assets.get(entry.getKey())
                          .price()
                          .multiply(entry.getValue())
                )
                .reduce(
                    BigDecimal.ZERO,
                    BigDecimal::add
                );
    }

    /**
     * Return a read-only snapshot of holdings.
     */
    public Map<String, BigDecimal> positions() {
        return Collections.unmodifiableMap(
            new HashMap<>(quantities)
        );
    }

    /**
     * Return a read-only snapshot of transaction history.
     */
    public List<Transaction> getTransactions() {
        return List.copyOf(transactions);
    }

    /**
     * Validate positive quantities.
     */
    private static void validateQuantity(
            BigDecimal quantity) {

        if (quantity == null ||
                quantity.signum() <= 0) {
            throw new IllegalArgumentException(
                "Quantity must be positive"
            );
        }
    }
}
