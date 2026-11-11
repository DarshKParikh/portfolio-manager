
package com.portfolio.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
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

    private static final MathContext MATH_CONTEXT =
            MathContext.DECIMAL128;

    private final Map<String, BigDecimal> quantities =
            new HashMap<>();

    private final Map<String, Asset> assets =
            new HashMap<>();

    private final List<Transaction> transactions =
            new ArrayList<>();

    // Weighted-average cost of shares currently held
    private final Map<String, BigDecimal> averageCosts =
            new HashMap<>();

    // Profit or loss from completed sales
    private BigDecimal realisedProfitLoss = BigDecimal.ZERO;

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

    // Import CSV transactions atomically
    public void importCsv(Path path) throws IOException {

        List<Transaction> imported =
                TransactionCsvReader.read(path);

        Portfolio preview = new Portfolio();

        preview.quantities.putAll(this.quantities);
        preview.assets.putAll(this.assets);
        preview.transactions.addAll(this.transactions);
        preview.averageCosts.putAll(this.averageCosts);

        preview.realisedProfitLoss = this.realisedProfitLoss;

        for (Transaction transaction : imported) {
            preview.applyTransaction(
                transaction,
                new Stock(
                    transaction.getSymbol(),
                    transaction.getPrice()
                )
            );
        }

        // Commit the changes only after every row succeeds
        quantities.clear();
        quantities.putAll(preview.quantities);

        assets.clear();
        assets.putAll(preview.assets);

        transactions.clear();
        transactions.addAll(preview.transactions);

        averageCosts.clear();
        averageCosts.putAll(preview.averageCosts);

        realisedProfitLoss = preview.realisedProfitLoss;
    }

    // Apply a trade and update cost basis
    private void applyTransaction(
            Transaction transaction,
            Asset asset) {

        String symbol = transaction.getSymbol();
        BigDecimal quantity = transaction.getQuantity();
        BigDecimal price = transaction.getPrice();

        if (transaction.getType() == TransactionType.BUY) {

            BigDecimal oldQuantity =
                    quantities.getOrDefault(
                        symbol,
                        BigDecimal.ZERO
                    );

            BigDecimal oldAverageCost =
                    averageCosts.getOrDefault(
                        symbol,
                        BigDecimal.ZERO
                    );

            BigDecimal newQuantity =
                    oldQuantity.add(quantity);

            BigDecimal totalCost =
                    oldQuantity.multiply(oldAverageCost)
                        .add(quantity.multiply(price));

            BigDecimal newAverageCost =
                    totalCost.divide(
                        newQuantity,
                        MATH_CONTEXT
                    );

            quantities.put(symbol, newQuantity);
            averageCosts.put(symbol, newAverageCost);
            assets.put(symbol, asset);

        } else if (transaction.getType() ==
                TransactionType.SELL) {

            BigDecimal held =
                    quantities.getOrDefault(
                        symbol,
                        BigDecimal.ZERO
                    );

            if (held.compareTo(quantity) < 0) {
                throw new IllegalArgumentException(
                    "Insufficient holdings for " + symbol
                );
            }

            BigDecimal averageCost =
                    averageCosts.get(symbol);

            BigDecimal saleProfit =
                    price.subtract(averageCost)
                         .multiply(quantity);

            BigDecimal remaining =
                    held.subtract(quantity);

            realisedProfitLoss =
                    realisedProfitLoss.add(saleProfit);

            if (remaining.signum() == 0) {
                quantities.remove(symbol);
                assets.remove(symbol);
                averageCosts.remove(symbol);
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

    // Update the latest market price
    public void updatePrice(Asset asset) {
        if (asset == null ||
                !assets.containsKey(asset.symbol())) {
            throw new IllegalArgumentException(
                "Asset must already be held"
            );
        }

        assets.put(asset.symbol(), asset);
    }

    // Total market value of current holdings
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

    // Profit/loss from shares already sold
    public BigDecimal realisedProfitLoss() {
        return realisedProfitLoss;
    }

    // Profit/loss from shares still held
    public BigDecimal unrealisedProfitLoss() {

        BigDecimal total = BigDecimal.ZERO;

        for (Map.Entry<String, BigDecimal> entry
                : quantities.entrySet()) {

            String symbol = entry.getKey();
            BigDecimal quantity = entry.getValue();

            BigDecimal marketPrice =
                    assets.get(symbol).price();

            BigDecimal averageCost =
                    averageCosts.get(symbol);

            BigDecimal profit =
                    marketPrice.subtract(averageCost)
                               .multiply(quantity);

            total = total.add(profit);
        }

        return total;
    }

    // Combined realised and unrealised P&L
    public BigDecimal totalProfitLoss() {
        return realisedProfitLoss()
                .add(unrealisedProfitLoss());
    }

    // Return a read-only snapshot of holdings
    public Map<String, BigDecimal> positions() {
        return Map.copyOf(quantities);
    }

    // Return a read-only snapshot of transactions
    public List<Transaction> getTransactions() {
        return List.copyOf(transactions);
    }

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
