package com.portfolio.app;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Scanner;

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

                switch (choice) {

                    case "1":
                        System.out.println(
                            "Buy functionality coming soon."
                        );
                        break;

                    case "2":
                        System.out.println(
                            "Sell functionality coming soon."
                        );
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
                        System.out.println(
                            "Price updates coming soon."
                        );
                        break;

                    case "7":
                        System.out.println(
                            "CSV import coming soon."
                        );
                        break;

                    case "8":
                        System.out.println(
                            "CSV export coming soon."
                        );
                        break;

                    case "9":
                        System.out.println(
                            "Transaction history coming soon."
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

    private static void viewHoldings(Portfolio portfolio) {

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
