package com.portfolio.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;


class TransactionTest {

    // Test 1: Valid transaction
    @Test
    void createsValidTransaction() {
        Transaction transaction = new Transaction(
            "AAPL",
            TransactionType.BUY,
            10,
            new BigDecimal("150.00"),
            LocalDate.of(2026, 1, 10)
        );

        assertEquals("AAPL", transaction.getSymbol());
        assertEquals(TransactionType.BUY, transaction.getType());
        assertEquals(10, transaction.getQuantity());
        assertEquals(
            new BigDecimal("150.00"),
            transaction.getPrice()
        );
        assertEquals(
            LocalDate.of(2026, 1, 10),
            transaction.getDate()
        );
    }

    // Test 2: Reject negative quantity
    @Test
    void rejectsNegativeQuantity() {
        assertThrows(
            IllegalArgumentException.class,
            () -> new Transaction(
                "AAPL",
                TransactionType.BUY,
                -5,
                new BigDecimal("150.00"),
                LocalDate.now()
            )
        );
    }

    // Test 3: Reject zero quantity
    @Test
    void rejectsZeroQuantity() {
        assertThrows(
            IllegalArgumentException.class,
            () -> new Transaction(
                "AAPL",
                TransactionType.BUY,
                0,
                new BigDecimal("150.00"),
                LocalDate.now()
            )
        );
    }

    // Test 4: Reject negative price
    @Test
    void rejectsNegativePrice() {
        assertThrows(
            IllegalArgumentException.class,
            () -> new Transaction(
                "AAPL",
                TransactionType.BUY,
                10,
                new BigDecimal("-50.00"),
                LocalDate.now()
            )
        );
    }

    // Test 5: Reject zero price
    @Test
    void rejectsZeroPrice() {
        assertThrows(
            IllegalArgumentException.class,
            () -> new Transaction(
                "AAPL",
                TransactionType.BUY,
                10,
                BigDecimal.ZERO,
                LocalDate.now()
            )
        );
    }

    // Test 6: Reject empty symbol
    @Test
    void rejectsEmptySymbol() {
        assertThrows(
            IllegalArgumentException.class,
            () -> new Transaction(
                "",
                TransactionType.BUY,
                10,
                new BigDecimal("150.00"),
                LocalDate.now()
            )
        );
    }

    // Test 7: Reject null symbol
    @Test
    void rejectsNullSymbol() {
        assertThrows(
            IllegalArgumentException.class,
            () -> new Transaction(
                null,
                TransactionType.BUY,
                10,
                new BigDecimal("150.00"),
                LocalDate.now()
            )
        );
    }

    // Test 8: Normalise stock symbol
    @Test
    void normalisesStockSymbol() {
        Transaction transaction = new Transaction(
            " aapl ",
            TransactionType.BUY,
            10,
            new BigDecimal("150.00"),
            LocalDate.now()
        );

        assertEquals("AAPL", transaction.getSymbol());
    }

    // Test 9: Reject null transaction type
    @Test
    void rejectsNullTransactionType() {
        assertThrows(
            NullPointerException.class,
            () -> new Transaction(
                "AAPL",
                null,
                10,
                new BigDecimal("150.00"),
                LocalDate.now()
            )
        );
    }

    // Test 10: Reject null date
    @Test
    void rejectsNullDate() {
        assertThrows(
            NullPointerException.class,
            () -> new Transaction(
                "AAPL",
                TransactionType.BUY,
                10,
                new BigDecimal("150.00"),
                null
            )
        );
    }

    // Test 11: Reject null price
    @Test
    void rejectsNullPrice() {
        assertThrows(
            IllegalArgumentException.class,
            () -> new Transaction(
                "AAPL",
                TransactionType.BUY,
                10,
                null,
                LocalDate.now()
            )
        );
    }

    // Test 12: Reject whitespace-only symbol
    @Test
    void rejectsBlankSymbol() {
        assertThrows(
            IllegalArgumentException.class,
            () -> new Transaction(
                "   ",
                TransactionType.BUY,
                10,
                new BigDecimal("150.00"),
                LocalDate.now()
            )
        );
    }
}
