
package com.portfolio.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.portfolio.model.Asset;
import com.portfolio.model.Transaction;

/**
 * In-memory portfolio with explicitly supplied asset prices.
 * No external market-data feed.
 */
public final class Portfolio {

    // Current holdings
    private final Map<String, BigDecimal> quantities =
            new HashMap<>();

    // Asset information and prices
    private final Map<String, Asset> assets =
            new HashMap<>();

    // Transaction history
    private final List<Transaction> transactions =
            new ArrayList<>();

    // Buy an asset
    public void buy(Asset asset, BigDecimal quantity) {
        validateQuantity(quantity);

        if (asset == null) {
            throw new IllegalArgumentException(
                "Asset is required"
            );
        }

        assets.put(asset.symbol(), asset);

        quantities.merge(
            asset.symbol(),
            quantity,
            BigDecimal::add
        );
    }

    // Sell an asset
    public void sell(String symbol, BigDecimal quantity) {
        validateQuantity(quantity);

        if (symbol == null) {
            throw new IllegalArgumentException(
                "Symbol is required"
            );
        }

        String key = symbol.trim()
                .toUpperCase(java.util.Locale.ROOT);

        BigDecimal held = quantities.getOrDefault(
            key, BigDecimal.ZERO
        );

        if (held.compareTo(quantity) < 0) {
            throw new IllegalArgumentException(
                "Insufficient holdings"
            );
        }

        BigDecimal remaining = held.subtract(quantity);

        if (remaining.signum() == 0) {
            quantities.remove(key);
            assets.remove(key);
        } else {
            quantities.put(key, remaining);
        }
    }

    // Update the market price of an existing asset
    public void updatePrice(Asset asset) {
        if (asset == null ||
                !assets.containsKey(asset.symbol())) {
            throw new IllegalArgumentException(
                "Asset must already be held"
            );
        }

        assets.put(asset.symbol(), asset);
    }

    // Calculate total portfolio market value
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

    // Return a read-only copy of current holdings
    public Map<String, BigDecimal> positions() {
        return Collections.unmodifiableMap(
            new HashMap<>(quantities)
        );
    }

    // Return a read-only snapshot of transaction history
    public List<Transaction> getTransactions() {
        return List.copyOf(transactions);
    }

    // Validate quantities
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
