# Portfolio Manager

A command-line investment portfolio management application built in Java. The application allows users to manage stock holdings, record trades, calculate portfolio valuations and analyse investment profit and loss.

This project was developed to explore financial software engineering, object-oriented design, financial data processing and automated testing.

## Features

- Buy and sell stocks with support for fractional shares.
- Track current holdings and complete transaction history.
- Calculate total portfolio market value.
- Calculate realised and unrealised profit and loss using weighted-average cost basis.
- Update asset market prices manually.
- Import historical transactions from CSV files.
- Export transaction history to CSV files.
- Validate trade quantities, prices and available holdings.
- Process CSV imports atomically to prevent partial updates when invalid transactions are encountered.
- Interactive command-line interface with input validation and error handling.

## Technologies

| Technology | Purpose |
|------------|---------|
| Java 17 | Application development |
| Maven | Build and dependency management |
| JUnit 5 | Unit and integration testing |
| BigDecimal | Financial calculations |
| Java NIO | CSV file handling |
| Git | Version control |

## Project Structure

```text
java-portfolio-manager/
├── pom.xml
├── README.md
├── transactions.csv
└── src/
    ├── main/java/com/portfolio/
    │   ├── app/
    │   │   └── Main.java
    │   ├── model/
    │   │   ├── Asset.java
    │   │   ├── Stock.java
    │   │   ├── Bond.java
    │   │   ├── Transaction.java
    │   │   └── TransactionType.java
    │   ├── service/
    │   │   └── Portfolio.java
    │   └── io/
    │       ├── TransactionCsvReader.java
    │       └── TransactionCsvWriter.java
    └── test/java/com/portfolio/
        ├── app/
        │   └── MainTest.java
        ├── model/
        │   └── TransactionTest.java
        ├── service/
        │   ├── PortfolioTest.java
        │   └── PortfolioProfitLossTest.java
        └── io/
            ├── TransactionCsvReaderTest.java
            └── TransactionCsvWriterTest.java
```

## Architecture

The application separates user interaction, financial business logic, domain models and file processing.

- **App:** Handles the command-line interface, user input and output.
- **Model:** Defines assets, stocks, bonds and immutable transaction records.
- **Service:** Manages holdings, applies transactions and performs financial calculations.
- **IO:** Handles CSV transaction import and export.

This separation makes the financial logic independently testable and allows the application to be extended without tightly coupling it to the user interface.

## Getting Started

### Prerequisites

- Java 17 or later
- Apache Maven

### Build the Project

Clone this repository and navigate to the project directory.

```bash
mvn clean package
```

### Run the Application

```bash
java -cp target/classes com.portfolio.app.Main
```

The application displays an interactive menu:

```text
====================================
       JAVA PORTFOLIO MANAGER
====================================

1. Buy an asset
2. Sell an asset
3. View holdings
4. View portfolio value
5. View profit and loss
6. Update asset price
7. Import transactions from CSV
8. Export transactions to CSV
9. View transaction history
0. Exit
```

## Example Usage

### Import Transactions

The repository includes a sample `transactions.csv` file.

```csv
symbol,type,quantity,price,date
AAPL,BUY,10,150.00,2026-01-15
MSFT,BUY,2.5,420.00,2026-01-16
AAPL,SELL,3,155.00,2026-01-20
```

Select option 7 and enter `transactions.csv` to import these transactions.

### View Holdings

```text
--- Current Holdings ---
AAPL: 7 units
MSFT: 2.5 units
```

### View Portfolio Value

```text
--- Portfolio Value ---
Total market value: 2135.00
```

### View Profit and Loss

```text
--- Profit and Loss ---
Realised P&L: 15.00
Unrealised P&L: 35.00
Total P&L: 50.00
```

The example uses illustrative financial data rather than live market prices.

## Financial Calculations

### Market Value

Portfolio market value is calculated as the sum of each holding's quantity multiplied by its current market price.

```text
Market Value = Sum(Quantity × Current Price)
```

### Weighted-Average Cost Basis

When additional shares are purchased, the average cost is recalculated based on the existing holding and the new purchase.

```text
New Average Cost =
    (Existing Quantity × Existing Average Cost
     + Purchased Quantity × Purchase Price)
    / Total Quantity
```

The average cost remains unchanged when part of a holding is sold.

### Realised Profit and Loss

Realised profit and loss represents gains or losses from completed sales.

```text
Realised P&L =
    (Sale Price - Average Cost) × Quantity Sold
```

### Unrealised Profit and Loss

Unrealised profit and loss represents gains or losses on assets that are still held.

```text
Unrealised P&L =
    (Current Price - Average Cost) × Remaining Quantity
```

### Total Profit and Loss

```text
Total P&L = Realised P&L + Unrealised P&L
```

Financial calculations use Java's `BigDecimal` rather than floating-point types to avoid binary floating-point representation errors. Weighted-average cost calculations use `MathContext.DECIMAL128` for division.

## Data Integrity

The application includes several safeguards:

- Rejects invalid quantities, prices and transaction types.
- Prevents selling more shares than are currently held.
- Preserves original transaction prices and dates.
- Returns read-only snapshots of holdings and transaction history.
- Validates CSV imports before committing portfolio changes.
- Requests confirmation before overwriting an existing export file.

## Testing

The project includes automated JUnit 5 tests covering:

- Transaction validation.
- Portfolio holdings and market valuation.
- Realised and unrealised profit and loss.
- Weighted-average cost calculations.
- Fractional share quantities.
- CSV import and export.
- CSV round-trip data preservation.
- Atomic import rollback.
- End-to-end command-line workflows and error handling.

Run the full test suite:

```bash
mvn clean test
```

Run the CLI integration tests only:

```bash
mvn -Dtest=MainTest test
```

## Current Limitations

- Market prices are entered manually rather than retrieved from a live market data provider.
- The CLI currently supports stock trading; bond models exist but are not exposed through the CLI.
- The application assumes a single currency and does not perform currency conversion.
- Transaction fees, dividends and taxes are not included in profit and loss calculations.
- Portfolio data is held in memory during each session.
- CSV exports preserve transaction history but not separate manual market price updates.
- CSV imports append transactions to the portfolio and do not automatically detect duplicate trades.
- CSV processing supports a fixed five-column format rather than general quoted CSV fields.

## Future Improvements

- Persistent storage using a relational database.
- Integration with a market data API.
- Multi-currency portfolio support.
- Portfolio allocation and performance analytics.
- A graphical or web-based user interface.

## Author

Darsh Parikh

Computer Science student at King's College London.

GitHub: https://github.com/DarshKParikh
