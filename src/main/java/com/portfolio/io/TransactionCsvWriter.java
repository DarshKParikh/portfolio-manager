package com.portfolio.io;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import com.portfolio.model.Transaction;

public final class TransactionCsvWriter {

    private static final String HEADER =
            "symbol,type,quantity,price,date";

    private TransactionCsvWriter() {
        // Utility class: no instances needed
    }

    /**
     * Export transactions to a CSV file.
     *
     * @param path destination file
     * @param transactions transactions to export
     * @throws IOException if writing fails
     */
    public static void write(
            Path path,
            List<Transaction> transactions
    ) throws IOException {

        if (path == null) {
            throw new IllegalArgumentException(
                "CSV file path is required"
            );
        }

        if (transactions == null) {
            throw new IllegalArgumentException(
                "Transaction list is required"
            );
        }

        // Validate before opening or changing the file.
        for (Transaction transaction : transactions) {
            if (transaction == null) {
                throw new IllegalArgumentException(
                    "Transaction list cannot contain null"
                );
            }
        }

        try (BufferedWriter writer =
                     Files.newBufferedWriter(path)) {

            // Write CSV column headings
            writer.write(HEADER);
            writer.newLine();

            // Write each transaction
            for (Transaction transaction : transactions) {

                writer.write(transaction.getSymbol());
                writer.write(",");

                writer.write(
                    transaction.getType().name()
                );
                writer.write(",");

                writer.write(
                    transaction.getQuantity()
                               .toPlainString()
                );
                writer.write(",");

                writer.write(
                    transaction.getPrice()
                               .toPlainString()
                );
                writer.write(",");

                writer.write(
                    transaction.getDate().toString()
                );

                writer.newLine();
            }
        }
    }
}
