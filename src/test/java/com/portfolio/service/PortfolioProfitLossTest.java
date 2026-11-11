package com.portfolio.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.portfolio.model.Stock;

class PortfolioProfitLossTest {

    @TempDir
    Path tempDir;

    // TEST 1: A new portfolio has no profit or loss
    @Test
    void emptyPortfolioHasZeroProfitLoss() {
        Portfolio p = new Portfolio();

        assertAmount("0", p.realisedProfitLoss());
        assertAmount("0", p.unrealisedProfitLoss());
        assertAmount("0", p.totalProfitLoss());
    }

    // TEST 2: A price increase creates unrealised profit
    @Test
    void calculatesUnrealisedProfit() {
        Portfolio p = new Portfolio();

        p.buy(
            new Stock("ABC", new BigDecimal("100")),
            new BigDecimal("10")
        );

        p.updatePrice(
            new Stock("ABC", new BigDecimal("120"))
        );

        assertAmount("200", p.unrealisedProfitLoss());
        assertAmount("0", p.realisedProfitLoss());
        assertAmount("200", p.totalProfitLoss());
    }

    // TEST 3: A price decrease creates unrealised loss
    @Test
    void calculatesUnrealisedLoss() {
        Portfolio p = new Portfolio();

        p.buy(
            new Stock("ABC", new BigDecimal("100")),
            new BigDecimal("5")
        );

        p.updatePrice(
            new Stock("ABC", new BigDecimal("80"))
        );

        assertAmount("-100", p.unrealisedProfitLoss());
        assertAmount("-100", p.totalProfitLoss());
    }

    // TEST 4: Selling at a higher price creates realised profit
    @Test
    void calculatesRealisedProfit() {
        Portfolio p = new Portfolio();

        p.buy(
            new Stock("ABC", new BigDecimal("100")),
            new BigDecimal("10")
        );

        p.updatePrice(
            new Stock("ABC", new BigDecimal("120"))
        );

        p.sell("ABC", new BigDecimal("4"));

        assertAmount("80", p.realisedProfitLoss());

        // Six shares remain at a market price of 120
        assertAmount("120", p.unrealisedProfitLoss());

        assertAmount("200", p.totalProfitLoss());
    }

    // TEST 5: Selling at a lower price creates realised loss
    @Test
    void calculatesRealisedLoss() {
        Portfolio p = new Portfolio();

        p.buy(
            new Stock("ABC", new BigDecimal("100")),
            new BigDecimal("5")
        );

        p.updatePrice(
            new Stock("ABC", new BigDecimal("90"))
        );

        p.sell("ABC", new BigDecimal("5"));

        assertAmount("-50", p.realisedProfitLoss());
        assertAmount("0", p.unrealisedProfitLoss());
        assertAmount("-50", p.totalProfitLoss());
    }

    // TEST 6: Multiple buys use weighted-average cost
    @Test
    void calculatesWeightedAverageCost() {
        Portfolio p = new Portfolio();

        p.buy(
            new Stock("ABC", new BigDecimal("100")),
            new BigDecimal("10")
        );

        p.buy(
            new Stock("ABC", new BigDecimal("120")),
            new BigDecimal("10")
        );

        // Total cost = 1000 + 1200 = 2200
        // Average cost = 2200 / 20 = 110
        // Current market price = 120

        assertAmount("200", p.unrealisedProfitLoss());

        p.sell("ABC", new BigDecimal("5"));

        // Realised profit = (120 - 110) * 5
        assertAmount("50", p.realisedProfitLoss());

        // Remaining unrealised profit = (120 - 110) * 15
        assertAmount("150", p.unrealisedProfitLoss());
    }

    // TEST 7: Fractional shares preserve precision
    @Test
    void calculatesFractionalShareProfit() {
        Portfolio p = new Portfolio();

        p.buy(
            new Stock("ABC", new BigDecimal("100.00")),
            new BigDecimal("2.5")
        );

        p.updatePrice(
            new Stock("ABC", new BigDecimal("110.00"))
        );

        assertAmount("25.000", p.unrealisedProfitLoss());
    }

    // TEST 8: Multiple assets contribute to total P&L
    @Test
    void calculatesProfitLossAcrossAssets() {
        Portfolio p = new Portfolio();

        p.buy(
            new Stock("ABC", new BigDecimal("100")),
            new BigDecimal("2")
        );

        p.buy(
            new Stock("XYZ", new BigDecimal("200")),
            new BigDecimal("3")
        );

        p.updatePrice(
            new Stock("ABC", new BigDecimal("120"))
        );

        p.updatePrice(
            new Stock("XYZ", new BigDecimal("180"))
        );

        // ABC: +40
        // XYZ: -60
        // Combined: -20

        assertAmount("-20", p.unrealisedProfitLoss());
        assertAmount("-20", p.totalProfitLoss());
    }

    // TEST 9: CSV imports calculate historical realised P&L
    @Test
    void calculatesProfitLossFromCsvImport()
            throws IOException {

        Path file = tempDir.resolve("profit-loss.csv");

        Files.writeString(file,
            "symbol,type,quantity,price,date\n" +
            "ABC,BUY,10,100.00,2026-01-15\n" +
            "ABC,SELL,4,120.00,2026-01-20\n"
        );

        Portfolio p = new Portfolio();
        p.importCsv(file);

        assertAmount("80", p.realisedProfitLoss());
        assertAmount("120", p.unrealisedProfitLoss());
        assertAmount("200", p.totalProfitLoss());
    }

    // TEST 10: Failed CSV imports preserve existing P&L
    @Test
    void failedImportPreservesProfitLoss()
            throws IOException {

        Portfolio p = new Portfolio();

        p.buy(
            new Stock("ABC", new BigDecimal("100")),
            new BigDecimal("5")
        );

        p.updatePrice(
            new Stock("ABC", new BigDecimal("120"))
        );

        p.sell("ABC", BigDecimal.ONE);

        // Existing realised P&L = 20
        // Existing unrealised P&L = 80

        Path file = tempDir.resolve("invalid.csv");

        Files.writeString(file,
            "symbol,type,quantity,price,date\n" +
            "XYZ,BUY,2,50.00,2026-01-15\n" +
            "XYZ,SELL,5,60.00,2026-01-16\n"
        );

        assertThrows(
            IllegalArgumentException.class,
            () -> p.importCsv(file)
        );

        assertAmount("20", p.realisedProfitLoss());
        assertAmount("80", p.unrealisedProfitLoss());
        assertAmount("100", p.totalProfitLoss());

        assertFalse(p.positions().containsKey("XYZ"));
        assertEquals(2, p.getTransactions().size());
    }

    // Helper: compare financial amounts without scale issues
    private static void assertAmount(
            String expected,
            BigDecimal actual) {

        assertEquals(
            0,
            new BigDecimal(expected).compareTo(actual),
            "Expected " + expected + " but got " + actual
        );
    }
}
