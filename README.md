# Java Portfolio Manager

A small, in-memory portfolio valuation application demonstrating Java 17, interfaces, encapsulation, polymorphism, collections, `BigDecimal` arithmetic and JUnit 5 tests.

## Requirements
- JDK 17 or newer
- Apache Maven 3.8+

## Run
```bash
mvn test
mvn exec:java
```

## Design
- `Asset` interface: shared contract for financial instruments.
- `Stock` and `Bond`: immutable implementations of `Asset`.
- `Portfolio`: holds quantities, validates buy/sell operations, updates prices and calculates current value.
- `Main`: runnable demonstration with **illustrative**, manually supplied prices.

## Limitations
- This is an in-memory educational project, not financial software.
- No persistence, live market feed, currency conversion, trade settlement, or transaction history.
- All valuations assume that the manually supplied prices use the same currency and unit conventions.

## Ideas to implement yourself
1. Add an immutable `Transaction` record and a transaction history.
2. Add CSV import/export and unit tests for malformed rows.
3. Build a command-line menu and support multiple portfolios.
4. Add average cost basis and realised/unrealised P&L.

## CV guidance
Only claim features you have personally run, tested, understood, and can explain in an interview. Extend the project yourself before describing it as an independent project.
