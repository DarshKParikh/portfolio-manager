package com.portfolio.app;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MainTest {

    @TempDir
    Path tempDir;

    // TEST 1: Application starts and exits correctly
    @Test
    void startsAndExitsSuccessfully() {

        String output = runCli(
            "0\n"
        );

        assertTrue(output.contains(
            "Welcome to Java Portfolio Manager!"
        ));

        assertTrue(output.contains(
            "Thank you for using Portfolio Manager!"
        ));
    }

    // TEST 2: Empty portfolio displays correctly
    @Test
    void displaysEmptyPortfolio() {

        String output = runCli(
            "3\n" +
            "4\n" +
            "5\n" +
            "0\n"
        );

        assertTrue(output.contains(
            "Your portfolio is currently empty."
        ));

        assertTrue(output.contains(
            "Total market value: 0"
        ));

        assertTrue(output.contains(
            "Realised P&L: 0"
        ));

        assertTrue(output.contains(
            "Unrealised P&L: 0"
        ));
    }

    // TEST 3: Buy and sell through the CLI
    @Test
    void buysAndSellsStocks() {

        String output = runCli(
            "1\n" +
            "AAPL\n" +
            "10\n" +
            "150\n" +
            "2\n" +
            "AAPL\n" +
            "3\n" +
            "3\n" +
            "4\n" +
            "0\n"
        );

        assertTrue(output.contains(
            "Successfully bought 10 units of AAPL."
        ));

        assertTrue(output.contains(
            "Successfully sold 3 units of AAPL."
        ));

        assertTrue(output.contains(
            "AAPL: 7 units"
        ));

        assertTrue(output.contains(
            "Total market value: 1050"
        ));
    }

    // TEST 4: Price updates and P&L calculations
    @Test
    void updatesPricesAndCalculatesProfitLoss() {

        String output = runCli(
            "1\n" +
            "AAPL\n" +
            "10\n" +
            "150\n" +
            "6\n" +
            "AAPL\n" +
            "170\n" +
            "2\n" +
            "AAPL\n" +
            "4\n" +
            "5\n" +
            "0\n"
        );

        assertTrue(output.contains(
            "Successfully updated AAPL price to 170."
        ));

        assertTrue(output.contains(
            "Realised P&L: 80"
        ));

        assertTrue(output.contains(
            "Unrealised P&L: 120"
        ));

        assertTrue(output.contains(
            "Total P&L: 200"
        ));
    }

    // TEST 5: Invalid menu choices do not crash the CLI
    @Test
    void handlesInvalidMenuChoice() {

        String output = runCli(
            "hello\n" +
            "0\n"
        );

        assertTrue(output.contains(
            "Invalid choice. Please try again."
        ));

        assertTrue(output.contains(
            "Thank you for using Portfolio Manager!"
        ));
    }

    // TEST 6: Invalid numeric input is handled safely
    @Test
    void rejectsInvalidQuantity() {

        String output = runCli(
            "1\n" +
            "AAPL\n" +
            "abc\n" +
            "3\n" +
            "0\n"
        );

        assertTrue(output.contains(
            "Error: Please enter a valid number."
        ));

        assertTrue(output.contains(
            "Your portfolio is currently empty."
        ));
    }

    // TEST 7: Overselling does not change holdings
    @Test
    void rejectsOverselling() {

        String output = runCli(
            "1\n" +
            "AAPL\n" +
            "5\n" +
            "150\n" +
            "2\n" +
            "AAPL\n" +
            "10\n" +
            "3\n" +
            "0\n"
        );

        assertTrue(output.contains(
            "Error: Insufficient holdings"
        ));

        assertTrue(output.contains(
            "AAPL: 5 units"
        ));
    }

    // TEST 8: Import and export transactions
    @Test
    void importsAndExportsCsv() throws IOException {

        Path inputFile =
                tempDir.resolve("transactions.csv");

        Path outputFile =
                tempDir.resolve("exported.csv");

        Files.writeString(
            inputFile,
            "symbol,type,quantity,price,date\n" +
            "AAPL,BUY,10,150.00,2026-01-15\n" +
            "AAPL,SELL,3,155.00,2026-01-20\n"
        );

        String output = runCli(
            "7\n" +
            inputFile + "\n" +
            "3\n" +
            "8\n" +
            outputFile + "\n" +
            "0\n"
        );

        assertTrue(output.contains(
            "Successfully imported 2 transactions."
        ));

        assertTrue(output.contains(
            "AAPL: 7 units"
        ));

        assertTrue(output.contains(
            "Successfully exported 2 transactions."
        ));

        assertTrue(Files.exists(outputFile));

        assertEquals(
            Files.readAllLines(inputFile),
            Files.readAllLines(outputFile)
        );
    }

    // TEST 9: Missing CSV files are handled safely
    @Test
    void handlesMissingCsvFile() {

        Path missingFile =
                tempDir.resolve("missing.csv");

        String output = runCli(
            "7\n" +
            missingFile + "\n" +
            "0\n"
        );

        assertTrue(output.contains(
            "File error:"
        ));

        assertTrue(output.contains(
            "Thank you for using Portfolio Manager!"
        ));
    }

    // TEST 10: Transaction history displays correctly
    @Test
    void displaysTransactionHistory() {

        String output = runCli(
            "1\n" +
            "MSFT\n" +
            "5\n" +
            "400\n" +
            "2\n" +
            "MSFT\n" +
            "2\n" +
            "9\n" +
            "0\n"
        );

        assertTrue(output.contains(
            "--- Transaction History ---"
        ));

        assertTrue(output.contains(
            "Total transactions: 2"
        ));

        assertTrue(output.contains("MSFT"));
        assertTrue(output.contains("BUY"));
        assertTrue(output.contains("SELL"));
    }

    // Helper: simulate terminal input and capture output
    private String runCli(String input) {

        InputStream originalIn = System.in;
        PrintStream originalOut = System.out;

        ByteArrayInputStream testInput =
                new ByteArrayInputStream(
                    input.getBytes(StandardCharsets.UTF_8)
                );

        ByteArrayOutputStream testOutput =
                new ByteArrayOutputStream();

        PrintStream capture = new PrintStream(
            testOutput,
            true,
            StandardCharsets.UTF_8
        );

        try {

            System.setIn(testInput);
            System.setOut(capture);

            Main.main(new String[0]);

            capture.flush();

            return testOutput.toString(
                StandardCharsets.UTF_8
            );

        } finally {

            System.setIn(originalIn);
            System.setOut(originalOut);

            capture.close();
        }
    }
}
