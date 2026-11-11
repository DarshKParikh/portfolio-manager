
package com.portfolio.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.portfolio.model.Transaction;
import com.portfolio.model.TransactionType;

public final class TransactionCsvReader {

    private TransactionCsvReader() {
        // Utility class: no instances needed
    }

    public static List<Transaction> read(Path path)
            throws IOException {

        if (path == null) {
            throw new IllegalArgumentException(
                "CSV file path is required"
            );
        }

        List<Transaction> transactions =
                new ArrayList<>();

        try (BufferedReader reader =
                     Files.newBufferedReader(path)) {

            String header = reader.readLine();

            if (header == null ||
                    !header.trim().equals(
                        "symbol,type,quantity,price,date"
                    )) {
                throw new IllegalArgumentException(
                    "Invalid CSV header"
                );
            }

            String line;
            int lineNumber = 1;

            while ((line = reader.readLine()) != null) {
                lineNumber++;

                if (line.isBlank()) {
                    continue;
                }

                String[] fields = line.split(",", -1);

                if (fields.length != 5) {
                    throw new IllegalArgumentException(
                        "Invalid column count on line "
                        + lineNumber
                    );
                }

                try {
                    String symbol = fields[0].trim();

                    TransactionType type =
                        TransactionType.valueOf(
                            fields[1].trim()
                                .toUpperCase(Locale.ROOT)
                        );

                    BigDecimal quantity =
                        new BigDecimal(fields[2].trim());

                    BigDecimal price =
                        new BigDecimal(fields[3].trim());

                    LocalDate date =
                        LocalDate.parse(fields[4].trim());

                    Transaction transaction =
                        new Transaction(
                            symbol,
                            type,
                            quantity,
                            price,
                            date
                        );

                    transactions.add(transaction);

                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException(
                        "Invalid transaction on line "
                        + lineNumber + ": "
                        + e.getMessage(),
                        e
                    );
                }
            }
        }

        return List.copyOf(transactions);
    }
}
