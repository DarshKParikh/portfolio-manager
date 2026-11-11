package com.portfolio.io;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.portfolio.model.Transaction;
import com.portfolio.model.TransactionType;

class TransactionCsvWriterTest {

    @TempDir
    Path tempDir;

    // TEST 1: Export transactions to a CSV file
    @Test
    void writesValidTransactions() throws IOException {
        Path file = tempDir.resolve("transactions.csv");

        Transaction buy = new Transaction(
            "AAPL",
            TransactionType.BUY,
            new BigDecimal("10"),
            new BigDecimal("150.00"),
            LocalDate.of(2026, 1, 15)
        );

        Transaction sell = new Transaction(
            "AAPL",
            TransactionType.SELL,
            new BigDecimal("3"),
            new BigDecimal("155.00"),
            LocalDate.of(2026, 1, 20)
        );

        TransactionCsvWriter.write(
            file,
            List.of(buy, sell)
        );

        String content = Files.readString(file);

        String expected =
            "symbol,type,quantity,price,date\n" +
            "AAPL,BUY,10,150.00,2026-01-15\n" +
            "AAPL,SELL,3,155.00,2026-01-20\n";

        assertEquals(
            expected.replace("\n", System.lineSeparator()),
            content
        );
    }

    // TEST 2: Empty lists produce a header-only CSV
    @Test
    void writesHeaderForEmptyList() throws IOException {
        Path file = tempDir.resolve("empty.csv");

        TransactionCsvWriter.write(file, List.of());

        List<String> lines = Files.readAllLines(file);

        assertEquals(1, lines.size());

        assertEquals(
            "symbol,type,quantity,price,date",
            lines.get(0)
        );
    }

    // TEST 3: Reject a null file path
    @Test
    void rejectsNullPath() {
        assertThrows(
            IllegalArgumentException.class,
            () -> TransactionCsvWriter.write(
                null,
                List.of()
            )
        );
    }

    // TEST 4: Reject a null transaction list
    @Test
    void rejectsNullTransactionList() {
        Path file = tempDir.resolve("null-list.csv");

        assertThrows(
            IllegalArgumentException.class,
            () -> TransactionCsvWriter.write(
                file,
                null
            )
        );
    }

    // TEST 5: Reject null transactions
    @Test
    void rejectsNullTransaction() {
        Path file = tempDir.resolve("null-transaction.csv");

        List<Transaction> transactions =
            Arrays.asList((Transaction) null);

        assertThrows(
            IllegalArgumentException.class,
            () -> TransactionCsvWriter.write(
                file,
                transactions
            )
        );

        assertFalse(Files.exists(file));
    }

    // TEST 6: Exported transactions can be imported again
    @Test
    void exportedTransactionsCanBeReadBack()
            throws IOException {

        Path file = tempDir.resolve("round-trip.csv");

        Transaction original = new Transaction(
            "MSFT",
            TransactionType.BUY,
            new BigDecimal("2.5"),
            new BigDecimal("420.75"),
            LocalDate.of(2026, 2, 10)
        );

        TransactionCsvWriter.write(
            file,
            List.of(original)
        );

        List<Transaction> imported =
            TransactionCsvReader.read(file);

        assertEquals(1, imported.size());

        Transaction restored = imported.get(0);

        assertEquals(
            original.getSymbol(),
            restored.getSymbol()
        );

        assertEquals(
            original.getType(),
            restored.getType()
        );

        assertEquals(
            0,
            original.getQuantity().compareTo(
                restored.getQuantity()
            )
        );

        assertEquals(
            0,
            original.getPrice().compareTo(
                restored.getPrice()
            )
        );

        assertEquals(
            original.getDate(),
            restored.getDate()
        );
    }

    // TEST 7: Preserve fractional quantities and prices
    @Test
    void preservesFinancialPrecision()
            throws IOException {

        Path file = tempDir.resolve("precision.csv");

        Transaction transaction = new Transaction(
            "AAPL",
            TransactionType.BUY,
            new BigDecimal("0.125"),
            new BigDecimal("187.4567"),
            LocalDate.of(2026, 3, 5)
        );

        TransactionCsvWriter.write(
            file,
            List.of(transaction)
        );

        String content = Files.readString(file);

        assertTrue(
            content.contains(
                "AAPL,BUY,0.125,187.4567,2026-03-05"
            )
        );
    }
}

