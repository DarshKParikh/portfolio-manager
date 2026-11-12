package com.portfolio.app;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;

import com.portfolio.io.TransactionCsvWriter;
import com.portfolio.model.Stock;
import com.portfolio.model.Transaction;
import com.portfolio.service.Portfolio;

public final class Main {

    private Main() {
    }

    public static void main(String[] args) {

        Portfolio portfolio = new Portfolio();

        try (Scanner scanner = new Scanner(System.in)) {

            boolean running = true;

            System.out.println(
                "Welcome to Java Portfolio Manager!"
            );

            while (running) {

                printMenu();

                System.out.print("Enter your choice: ");

                if (!scanner.hasNextLine()) {
                    break;
                }

                String choice = scanner.nextLine().trim();

                try {

                    switch (choice) {

                        case "1":
                            buyAsset(portfolio, scanner);
                            break;

                        case "2":
                            sellAsset(portfolio, scanner);
                            break;

                        case "3":
                            viewHoldings(portfolio);
                            break;

                        case "4":
                            viewPortfolioValue(portfolio);
                            break;

                        case "5":
                            viewProfitLoss(portfolio);
                            break;

                        case "6":
                            updateAssetPrice(
                                portfolio, scanner
                            );
                            break;

                        case "7":
                            importTransactions(
                                portfolio, scanner
                            );
                            break;

                        case "8":
                            exportTransactions(
                                portfolio, scanner
                            );
                            break;

                        case "9":
                            viewTransactionHistory(
                                portfolio
                            );
                            break;

                        case "0":
                            running = false;

                            System.out.println(
                                "Thank you for using Portfolio Manager!"
                            );
                            break;

                        default:
                            System.out.println(
                                "Invalid choice. Please try again."
                            );
                    }

                } catch (IllegalArgumentException e) {

                    System.out.println(
                        "Error: " + e.getMessage()
                    );

                } catch (IOException e) {

                    System.out.println(
                        "File error: " + e.getMessage()
                    );
                }
            }
        }
    }

    private static void printMenu() {

        System.out.println();
        System.out.println("====================================");
        System.out.println("       JAVA PORTFOLIO MANAGER");
        System.out.println("====================================");

        System.out.println("1. Buy an asset");
        System.out.println("2. Sell an asset");
        System.out.println("3. View holdings");
        System.out.println("4. View portfolio value");
        System.out.println("5. View profit and loss");
        System.out.println("6. Update asset price");
        System.out.println("7. Import transactions from CSV");
        System.out.println("8. Export transactions to CSV");
        System.out.println("9. View transaction history");
        System.out.println("0. Exit");

        System.out.println();
    }

    private static void buyAsset(
            Portfolio portfolio,
            Scanner scanner) {

        System.out.println();
        System.out.println("--- Buy an Asset ---");

        String symbol = readSymbol(scanner);

        BigDecimal quantity = readPositiveDecimal(
            scanner,
            "Enter quantity: "
        );

        BigDecimal price = readPositiveDecimal(
            scanner,
            "Enter price per share: "
        );

        Stock stock = new Stock(symbol, price);

        portfolio.buy(stock, quantity);

        System.out.println(
            "Successfully bought " +
            quantity.toPlainString() +
            " units of " + symbol + "."
        );
    }

    private static void sellAsset(
            Portfolio portfolio,
            Scanner scanner) {

        System.out.println();
        System.out.println("--- Sell an Asset ---");

        String symbol = readSymbol(scanner);

        BigDecimal quantity = readPositiveDecimal(
            scanner,
            "Enter quantity: "
        );

        portfolio.sell(symbol, quantity);

        System.out.println(
            "Successfully sold " +
            quantity.toPlainString() +
            " units of " + symbol + "."
        );
    }

    private static void updateAssetPrice(
            Portfolio portfolio,
            Scanner scanner) {

        System.out.println();
        System.out.println("--- Update Asset Price ---");

        String symbol = readSymbol(scanner);

        BigDecimal newPrice = readPositiveDecimal(
            scanner,
            "Enter new price: "
        );

        portfolio.updatePrice(
            new Stock(symbol, newPrice)
        );

        System.out.println(
            "Successfully updated " +
            symbol + " price to " +
            newPrice.toPlainString() + "."
        );
    }

    // NEW: Import transactions from a CSV file
    private static void importTransactions(
            Portfolio portfolio,
            Scanner scanner) throws IOException {

        System.out.println();
        System.out.println("--- Import Transactions ---");

        Path path = readPath(
            scanner,
            "Enter CSV file path: "
        );

        int previousCount =
                portfolio.getTransactions().size();

        portfolio.importCsv(path);

        int importedCount =
                portfolio.getTransactions().size()
                - previousCount;

        System.out.println(
            "Successfully imported " +
            importedCount + " transactions."
        );
    }

    // NEW: Export transaction history to a CSV file
    private static void exportTransactions(
            Portfolio portfolio,
            Scanner scanner) throws IOException {

        System.out.println();
        System.out.println("--- Export Transactions ---");

        Path path = readPath(
            scanner,
            "Enter destination file path: "
        );

        // Prevent accidental overwriting
        if (Files.exists(path)) {

            String confirmation = readInput(
                scanner,
                "File already exists. Overwrite? (y/n): "
            );

            if (!confirmation.equalsIgnoreCase("y")) {

                System.out.println("Export cancelled.");
                return;
            }
        }

        List<Transaction> transactions =
                portfolio.getTransactions();

        TransactionCsvWriter.write(
            path,
            transactions
        );

        System.out.println(
            "Successfully exported " +
            transactions.size() + " transactions."
        );

        System.out.println(
            "Saved to: " +
            path.toAbsolutePath().normalize()
        );
    }

    private static void viewTransactionHistory(
            Portfolio portfolio) {

        List<Transaction> transactions =
                portfolio.getTransactions();

        System.out.println();
        System.out.println("--- Transaction History ---");

        if (transactions.isEmpty()) {

            System.out.println(
                "No transactions recorded yet."
            );

            return;
        }

        System.out.printf(
            "%-12s %-10s %-8s %12s %12s%n",
            "DATE",
            "SYMBOL",
            "TYPE",
            "QUANTITY",
            "PRICE"
        );

        System.out.println(
            "----------------------------------------------------------"
        );

        for (Transaction transaction : transactions) {

            System.out.printf(
                "%-12s %-10s %-8s %12s %12s%n",
                transaction.getDate(),
                transaction.getSymbol(),
                transaction.getType(),
                transaction.getQuantity().toPlainString(),
                transaction.getPrice().toPlainString()
            );
        }

        System.out.println();

        System.out.println(
            "Total transactions: " +
            transactions.size()
        );
    }

    private static String readSymbol(Scanner scanner) {

        String symbol = readInput(
            scanner,
            "Enter stock symbol: "
        ).toUpperCase(Locale.ROOT);

        if (symbol.isBlank()) {

            throw new IllegalArgumentException(
                "Stock symbol is required."
            );
        }

        return symbol;
    }

    private static BigDecimal readPositiveDecimal(
            Scanner scanner,
            String prompt) {

        String input = readInput(scanner, prompt);

        BigDecimal value;

        try {

            value = new BigDecimal(input);

        } catch (NumberFormatException e) {

            throw new IllegalArgumentException(
                "Please enter a valid number."
            );
        }

        if (value.signum() <= 0) {

            throw new IllegalArgumentException(
                "Value must be greater than zero."
            );
        }

        return value;
    }

    // NEW: Read and validate a file path
    private static Path readPath(
            Scanner scanner,
            String prompt) {

        String input = readInput(scanner, prompt);

        if (input.isBlank()) {

            throw new IllegalArgumentException(
                "File path is required."
            );
        }

        return Path.of(input);
    }

    private static String readInput(
            Scanner scanner,
            String prompt) {

        System.out.print(prompt);

        if (!scanner.hasNextLine()) {

            throw new IllegalArgumentException(
                "No input provided."
            );
        }

        return scanner.nextLine().trim();
    }

    private static void viewHoldings(
            Portfolio portfolio) {

        Map<String, BigDecimal> positions =
                portfolio.positions();

        System.out.println();
        System.out.println("--- Current Holdings ---");

        if (positions.isEmpty()) {

            System.out.println(
                "Your portfolio is currently empty."
            );

            return;
        }

        for (Map.Entry<String, BigDecimal> entry
                : positions.entrySet()) {

            System.out.println(
                entry.getKey() + ": " +
                entry.getValue().toPlainString() +
                " units"
            );
        }
    }

    private static void viewPortfolioValue(
            Portfolio portfolio) {

        System.out.println();
        System.out.println("--- Portfolio Value ---");

        System.out.println(
            "Total market value: " +
            portfolio.marketValue().toPlainString()
        );
    }

    private static void viewProfitLoss(
            Portfolio portfolio) {

        System.out.println();
        System.out.println("--- Profit and Loss ---");

        System.out.println(
            "Realised P&L: " +
            portfolio.realisedProfitLoss().toPlainString()
        );

        System.out.println(
            "Unrealised P&L: " +
            portfolio.unrealisedProfitLoss().toPlainString()
        );

        System.out.println(
            "Total P&L: " +
            portfolio.totalProfitLoss().toPlainString()
        );
    }
}
