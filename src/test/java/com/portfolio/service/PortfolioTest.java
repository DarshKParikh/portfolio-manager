package com.portfolio.service;

import com.portfolio.model.Stock;
import com.portfolio.model.Transaction;
import com.portfolio.model.TransactionType;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PortfolioTest {

    @TempDir
    Path tempDir;

    // ORIGINAL TEST 1
    @Test
    void calculatesValueAfterBuyAndSell() {
        Portfolio p = new Portfolio();
        p.buy(new Stock("ABC", new BigDecimal("10.25")), new BigDecimal("4"));
        assertEquals(0, p.marketValue().compareTo(new BigDecimal("41.00")));
        p.sell("ABC", new BigDecimal("1"));
        assertEquals(0, p.marketValue().compareTo(new BigDecimal("30.75")));
    }

    // ORIGINAL TEST 2
    @Test
    void rejectsOverselling() {
        Portfolio p = new Portfolio();
        p.buy(new Stock("ABC", BigDecimal.TEN), BigDecimal.ONE);
        assertThrows(
            IllegalArgumentException.class,
            () -> p.sell("ABC", new BigDecimal("2"))
        );
        assertEquals(BigDecimal.ONE, p.positions().get("ABC"));
    }

    // ORIGINAL TEST 3
    @Test
    void rejectsInvalidQuantitiesAndPrices() {
        Portfolio p = new Portfolio();
        assertThrows(
            IllegalArgumentException.class,
            () -> p.buy(
                new Stock("ABC", BigDecimal.ONE),
                BigDecimal.ZERO
            )
        );
        assertThrows(
            IllegalArgumentException.class,
            () -> new Stock("ABC", new BigDecimal("-1"))
        );
    }

    // ORIGINAL TEST 4
    @Test
    void repricesExistingHolding() {
        Portfolio p = new Portfolio();
        p.buy(
            new Stock("ABC", new BigDecimal("10")),
            BigDecimal.ONE
        );
        p.updatePrice(
            new Stock("ABC", new BigDecimal("12"))
        );
        assertEquals(
            0,
            p.marketValue().compareTo(new BigDecimal("12"))
        );
    }

    // EXISTING TEST 5
    @Test
    void buyingRecordsTransaction() {
        Portfolio p = new Portfolio();

        p.buy(
            new Stock("ABC", new BigDecimal("10.25")),
            new BigDecimal("4")
        );

        List<Transaction> history = p.getTransactions();

        assertEquals(1, history.size());

        Transaction transaction = history.get(0);

        assertEquals(TransactionType.BUY, transaction.getType());
        assertEquals("ABC", transaction.getSymbol());

        assertEquals(
            0,
            new BigDecimal("4").compareTo(
                transaction.getQuantity()
            )
        );

        assertEquals(
            0,
            new BigDecimal("10.25").compareTo(
                transaction.getPrice()
            )
        );
    }

    // EXISTING TEST 6
    @Test
    void sellingRecordsTransaction() {
        Portfolio p = new Portfolio();

        p.buy(
            new Stock("ABC", BigDecimal.TEN),
            new BigDecimal("5")
        );

        p.sell("ABC", new BigDecimal("2"));

        List<Transaction> history = p.getTransactions();

        assertEquals(2, history.size());

        assertEquals(
            TransactionType.BUY,
            history.get(0).getType()
        );

        assertEquals(
            TransactionType.SELL,
            history.get(1).getType()
        );

        assertEquals(
            0,
            new BigDecimal("2").compareTo(
                history.get(1).getQuantity()
            )
        );

        assertEquals(
            0,
            new BigDecimal("3").compareTo(
                p.positions().get("ABC")
            )
        );
    }

    // EXISTING TEST 7
    @Test
    void failedSaleDoesNotChangeHistory() {
        Portfolio p = new Portfolio();

        p.buy(
            new Stock("ABC", BigDecimal.TEN),
            BigDecimal.ONE
        );

        assertThrows(
            IllegalArgumentException.class,
            () -> p.sell("ABC", new BigDecimal("5"))
        );

        assertEquals(1, p.getTransactions().size());

        assertEquals(
            0,
            BigDecimal.ONE.compareTo(
                p.positions().get("ABC")
            )
        );
    }

    // EXISTING TEST 8
    @Test
    void recordsFractionalShares() {
        Portfolio p = new Portfolio();

        p.buy(
            new Stock("ABC", new BigDecimal("100.00")),
            new BigDecimal("2.5")
        );

        Transaction transaction =
            p.getTransactions().get(0);

        assertEquals(
            0,
            new BigDecimal("2.5").compareTo(
                transaction.getQuantity()
            )
        );

        assertEquals(
            0,
            new BigDecimal("250.00").compareTo(
                p.marketValue()
            )
        );
    }

    // EXISTING TEST 9
    @Test
    void transactionHistoryIsReadOnly() {
        Portfolio p = new Portfolio();

        p.buy(
            new Stock("ABC", BigDecimal.TEN),
            BigDecimal.ONE
        );

        List<Transaction> history = p.getTransactions();

        assertThrows(
            UnsupportedOperationException.class,
            () -> history.clear()
        );

        assertEquals(1, p.getTransactions().size());
    }

    // EXISTING TEST 10
    @Test
    void sellingAllSharesPreservesHistory() {
        Portfolio p = new Portfolio();

        p.buy(
            new Stock("ABC", BigDecimal.TEN),
            new BigDecimal("5")
        );

        p.sell("ABC", new BigDecimal("5"));

        assertFalse(
            p.positions().containsKey("ABC")
        );

        assertEquals(2, p.getTransactions().size());

        assertEquals(
            TransactionType.SELL,
            p.getTransactions().get(1).getType()
        );
    }

    // NEW TEST 11: Import valid CSV transactions
    @Test
    void importsValidCsvTransactions() throws IOException {
        Path file = tempDir.resolve("valid.csv");

        Files.writeString(file,
            "symbol,type,quantity,price,date\n" +
            "AAPL,BUY,10,150.00,2026-01-15\n" +
            "MSFT,BUY,2.5,420.00,2026-01-16\n" +
            "AAPL,SELL,3,155.00,2026-01-20\n"
        );

        Portfolio p = new Portfolio();
        p.importCsv(file);

        assertEquals(
            0,
            new BigDecimal("7").compareTo(
                p.positions().get("AAPL")
            )
        );

        assertEquals(
            0,
            new BigDecimal("2.5").compareTo(
                p.positions().get("MSFT")
            )
        );

        assertEquals(3, p.getTransactions().size());
    }

    // NEW TEST 12: Preserve historical dates and prices
    @Test
    void preservesImportedTransactionDetails()
            throws IOException {

        Path file = tempDir.resolve("historical.csv");

        Files.writeString(file,
            "symbol,type,quantity,price,date\n" +
            "AAPL,BUY,10,150.00,2026-01-15\n"
        );

        Portfolio p = new Portfolio();
        p.importCsv(file);

        Transaction transaction =
            p.getTransactions().get(0);

        assertEquals(
            LocalDate.of(2026, 1, 15),
            transaction.getDate()
        );

        assertEquals(
            0,
            new BigDecimal("150.00").compareTo(
                transaction.getPrice()
            )
        );
    }

    // NEW TEST 13: Reject invalid imports atomically
    @Test
    void invalidImportDoesNotChangePortfolio()
            throws IOException {

        Path file = tempDir.resolve("invalid-sale.csv");

        Files.writeString(file,
            "symbol,type,quantity,price,date\n" +
            "AAPL,BUY,5,150.00,2026-01-15\n" +
            "AAPL,SELL,10,155.00,2026-01-16\n"
        );

        Portfolio p = new Portfolio();

        p.buy(
            new Stock("MSFT", new BigDecimal("400")),
            BigDecimal.ONE
        );

        assertThrows(
            IllegalArgumentException.class,
            () -> p.importCsv(file)
        );

        // Original holdings remain unchanged
        assertEquals(
            BigDecimal.ONE,
            p.positions().get("MSFT")
        );

        assertFalse(
            p.positions().containsKey("AAPL")
        );

        // Original transaction history remains unchanged
        assertEquals(1, p.getTransactions().size());
    }

    // NEW TEST 14: Import into an existing portfolio
    @Test
    void importsIntoExistingPortfolio()
            throws IOException {

        Path file = tempDir.resolve("existing.csv");

        Files.writeString(file,
            "symbol,type,quantity,price,date\n" +
            "AAPL,BUY,3,160.00,2026-01-20\n"
        );

        Portfolio p = new Portfolio();

        p.buy(
            new Stock("AAPL", new BigDecimal("150")),
            new BigDecimal("5")
        );

        p.importCsv(file);

        assertEquals(
            0,
            new BigDecimal("8").compareTo(
                p.positions().get("AAPL")
            )
        );

        assertEquals(2, p.getTransactions().size());
    }
}
