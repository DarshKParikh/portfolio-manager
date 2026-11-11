
package com.portfolio.service;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

import com.portfolio.model.Stock;
import com.portfolio.model.Transaction;
import com.portfolio.model.TransactionType;

class PortfolioTest {

    // ORIGINAL TEST 1: Calculate value after buying and selling
    @Test
    void calculatesValueAfterBuyAndSell() {
        Portfolio p = new Portfolio();
        p.buy(new Stock("ABC", new BigDecimal("10.25")), new BigDecimal("4"));
        assertEquals(0, p.marketValue().compareTo(new BigDecimal("41.00")));
        p.sell("ABC", new BigDecimal("1"));
        assertEquals(0, p.marketValue().compareTo(new BigDecimal("30.75")));
    }

    // ORIGINAL TEST 2: Reject selling more than owned
    @Test
    void rejectsOverselling() {
        Portfolio p = new Portfolio();
        p.buy(new Stock("ABC", BigDecimal.TEN), BigDecimal.ONE);
        assertThrows(IllegalArgumentException.class, () -> p.sell("ABC", new BigDecimal("2")));
        assertEquals(BigDecimal.ONE, p.positions().get("ABC"));
    }

    // ORIGINAL TEST 3: Reject invalid quantities and prices
    @Test
    void rejectsInvalidQuantitiesAndPrices() {
        Portfolio p = new Portfolio();
        assertThrows(IllegalArgumentException.class, () -> p.buy(new Stock("ABC", BigDecimal.ONE), BigDecimal.ZERO));
        assertThrows(IllegalArgumentException.class, () -> new Stock("ABC", new BigDecimal("-1")));
    }

    // ORIGINAL TEST 4: Update market price
    @Test
    void repricesExistingHolding() {
        Portfolio p = new Portfolio();
        p.buy(new Stock("ABC", new BigDecimal("10")), BigDecimal.ONE);
        p.updatePrice(new Stock("ABC", new BigDecimal("12")));
        assertEquals(0, p.marketValue().compareTo(new BigDecimal("12")));
    }

    // NEW TEST 5: Buying records a transaction
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

    // NEW TEST 6: Selling records a transaction
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

    // NEW TEST 7: Failed sales must not change history
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

    // NEW TEST 8: Support fractional share transactions
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

    // NEW TEST 9: History cannot be modified externally
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

    // NEW TEST 10: Selling all shares preserves history
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
}
