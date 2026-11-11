
package com.portfolio.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.portfolio.io.TransactionCsvReader;
import com.portfolio.model.Asset;
import com.portfolio.model.Stock;
import com.portfolio.model.Transaction;
import com.portfolio.model.TransactionType;

public final class Portfolio {

    private final Map<String, BigDecimal> quantities =
            new HashMap<>();

    private final Map<String, Asset> assets =
            new HashMap<>();

    private final List<Transaction> transactions =
            new ArrayList<>();

    // Buy an asset and record the transaction
    public void buy(Asset asset, BigDecimal quantity) {
        validateQuantity(quantity);

        if (asset == null) {
            throw new IllegalArgumentException(
                "Asset is required"
            );
        }

        Transaction transaction = new Transaction(
            asset.symbol(),
            TransactionType.BUY,
            quantity,
            asset.price(),
            LocalDate.now()
        );

        applyTransaction(transaction, asset);
    }

    // Sell an asset and record the transaction
    public void sell(String symbol, BigDecimal quantity) {
        validateQuantity(quantity);

        if (symbol == null || symbol.isBlank()) {
            throw new IllegalArgumentException(
                "Symbol is required"
            );
        }

        String key = symbol.trim()
                .toUpperCase(Locale.ROOT);

        Asset asset = assets.get(key);

        if (asset == null) {
            throw new IllegalArgumentException(
                "Insufficient holdings"
            );
        }

        Transaction transaction = new Transaction(
            key,
            TransactionType.SELL,
            quantity,
            asset.price(),
            LocalDate.now()
        );

        applyTransaction(transaction, asset);
    }

    // Import transactions from a CSV file
    public void importCsv(Path path) throws IOException {

        List<Transaction> imported =
                TransactionCsvReader.read(path);

        // Validate the entire batch using a temporary
        // copy of the portfolio before changing anything.
        Portfolio preview = new Portfolio();

        preview.quantities.putAll(this.quantities);
        preview.assets.putAll(this.assets);
        preview.transactions.addAll(this.transactions);

        for (Transaction transaction : imported) {
            preview.applyTransaction(
                transaction,
                new Stock(
                    transaction.getSymbol(),
                    transaction.getPrice()
                )
            );
        }

        // All transactions passed validation.
        // Apply the resulting state atomically.
        quantities.clear();
        quantities.putAll(preview.quantities);

        assets.clear();
        assets.putAll(preview.assets);

        transactions.clear();
        transactions.addAll(preview.transactions);
    }

    // Apply a transaction to portfolio holdings
    private void applyTransaction(
            Transaction transaction,
            Asset asset) {

        String symbol = transaction.getSymbol();
        BigDecimal quantity = transaction.getQuantity();

        if (transaction.getType() == TransactionType.BUY) {

            quantities.merge(
                symbol,
                quantity,
                BigDecimal::add
            );

            assets.put(symbol, asset);

        } else if (transaction.getType() == TransactionType.SELL) {

            BigDecimal held = quantities.getOrDefault(
                symbol,
                BigDecimal.ZERO
            );

            if (held.compareTo(quantity) < 0) {
                throw new IllegalArgumentException(
                    "Insufficient holdings for " + symbol
                );
            }

            BigDecimal remaining = held.subtract(quantity);

            if (remaining.signum() == 0) {
                quantities.remove(symbol);
                assets.remove(symbol);
            } else {
                quantities.put(symbol, remaining);
                assets.put(symbol, asset);
            }

        } else {
            throw new IllegalArgumentException(
                "Unsupported transaction type"
            );
        }

        transactions.add(transaction);
    }

    // Update the price of an existing holding
    public void updatePrice(Asset asset) {
        if (asset == null ||
                !assets.containsKey(asset.symbol())) {
            throw new IllegalArgumentException(
                "Asset must already be held"
            );
        }

        assets.put(asset.symbol(), asset);
    }

    // Calculate the total market value
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

    // Return a read-only snapshot of positions
    public Map<String, BigDecimal> positions() {
        return Map.copyOf(quantities);
    }

    // Return a read-only snapshot of transactions
    public List<Transaction> getTransactions() {
        return List.copyOf(transactions);
    }

    // Validate share quantities
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
