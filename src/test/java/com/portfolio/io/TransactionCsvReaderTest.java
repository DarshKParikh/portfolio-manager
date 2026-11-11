
package com.portfolio.io;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.portfolio.model.Transaction;
import com.portfolio.model.TransactionType;

class TransactionCsvReaderTest {

    @TempDir
    Path tempDir;

    // TEST 1: Read valid transactions
    @Test
    void readsValidTransactions() throws IOException {
        Path file = tempDir.resolve("transactions.csv");

        Files.writeString(file,
            "symbol,type,quantity,price,date\n" +
            "AAPL,BUY,10,150.00,2026-01-15\n" +
            "MSFT,SELL,2.5,420.00,2026-01-16\n"
        );

        List<Transaction> transactions =
            TransactionCsvReader.read(file);

        assertEquals(2, transactions.size());

        Transaction first = transactions.get(0);

        assertEquals("AAPL", first.getSymbol());
        assertEquals(TransactionType.BUY, first.getType());

        assertEquals(
            0,
            new BigDecimal("10").compareTo(
                first.getQuantity()
            )
        );

        assertEquals(
            0,
            new BigDecimal("150.00").compareTo(
                first.getPrice()
            )
        );

        assertEquals(
            LocalDate.of(2026, 1, 15),
            first.getDate()
        );

        Transaction second = transactions.get(1);

        assertEquals("MSFT", second.getSymbol());
        assertEquals(TransactionType.SELL, second.getType());

        assertEquals(
            0,
            new BigDecimal("2.5").compareTo(
                second.getQuantity()
            )
        );
    }

    // TEST 2: Reject invalid CSV header
    @Test
    void rejectsInvalidHeader() throws IOException {
        Path file = tempDir.resolve("invalid-header.csv");

        Files.writeString(file,
            "ticker,action,amount\n" +
            "AAPL,BUY,10\n"
        );

        IllegalArgumentException exception =
            assertThrows(
                IllegalArgumentException.class,
                () -> TransactionCsvReader.read(file)
            );

        assertTrue(
            exception.getMessage().contains(
                "Invalid CSV header"
            )
        );
    }

    // TEST 3: Reject negative quantities
    @Test
    void rejectsInvalidQuantity() throws IOException {
        Path file = tempDir.resolve("invalid-quantity.csv");

        Files.writeString(file,
            "symbol,type,quantity,price,date\n" +
            "AAPL,BUY,-5,150.00,2026-01-15\n"
        );

        IllegalArgumentException exception =
            assertThrows(
                IllegalArgumentException.class,
                () -> TransactionCsvReader.read(file)
            );

        assertTrue(
            exception.getMessage().contains("line 2")
        );
    }

    // TEST 4: Reject invalid transaction types
    @Test
    void rejectsInvalidTransactionType() throws IOException {
        Path file = tempDir.resolve("invalid-type.csv");

        Files.writeString(file,
            "symbol,type,quantity,price,date\n" +
            "AAPL,HOLD,10,150.00,2026-01-15\n"
        );

        assertThrows(
            IllegalArgumentException.class,
            () -> TransactionCsvReader.read(file)
        );
    }

    // TEST 5: Reject missing CSV columns
    @Test
    void rejectsMissingColumns() throws IOException {
        Path file = tempDir.resolve("missing-columns.csv");

        Files.writeString(file,
            "symbol,type,quantity,price,date\n" +
            "AAPL,BUY,10,150.00\n"
        );

        IllegalArgumentException exception =
            assertThrows(
                IllegalArgumentException.class,
                () -> TransactionCsvReader.read(file)
            );

        assertTrue(
            exception.getMessage().contains("line 2")
        );
    }

    // TEST 6: Reject invalid dates
    @Test
    void rejectsInvalidDate() throws IOException {
        Path file = tempDir.resolve("invalid-date.csv");

        Files.writeString(file,
            "symbol,type,quantity,price,date\n" +
            "AAPL,BUY,10,150.00,not-a-date\n"
        );

        IllegalArgumentException exception =
            assertThrows(
                IllegalArgumentException.class,
                () -> TransactionCsvReader.read(file)
            );

        assertTrue(
            exception.getMessage().contains("line 2")
        );
    }

    // TEST 7: Skip blank lines
    @Test
    void skipsBlankLines() throws IOException {
        Path file = tempDir.resolve("blank-lines.csv");

        Files.writeString(file,
            "symbol,type,quantity,price,date\n" +
            "\n" +
            "AAPL,BUY,10,150.00,2026-01-15\n" +
            "\n"
        );

        List<Transaction> transactions =
            TransactionCsvReader.read(file);

        assertEquals(1, transactions.size());
    }

    // TEST 8: Return an unmodifiable list
    @Test
    void returnsReadOnlyList() throws IOException {
        Path file = tempDir.resolve("read-only.csv");

        Files.writeString(file,
            "symbol,type,quantity,price,date\n" +
            "AAPL,BUY,10,150.00,2026-01-15\n"
        );

        List<Transaction> transactions =
            TransactionCsvReader.read(file);

        assertThrows(
            UnsupportedOperationException.class,
            () -> transactions.clear()
        );
    }
}
