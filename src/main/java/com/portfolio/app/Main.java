package com.portfolio.app;

import com.portfolio.model.Bond;
import com.portfolio.model.Stock;
import com.portfolio.service.Portfolio;
import java.math.BigDecimal;

public class Main {
    public static void main(String[] args) {
        Portfolio portfolio = new Portfolio();
        portfolio.buy(new Stock("AAPL", new BigDecimal("190.50")), new BigDecimal("3"));
        portfolio.buy(new Bond("US-TREASURY", new BigDecimal("98.25"), new BigDecimal("0.04")), new BigDecimal("2"));
        System.out.println("Positions: " + portfolio.positions());
        System.out.println("Market value (illustrative USD): $" + portfolio.marketValue());
        portfolio.sell("AAPL", BigDecimal.ONE);
        System.out.println("After sale: $" + portfolio.marketValue());
    }
}
