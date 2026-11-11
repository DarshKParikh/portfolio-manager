package com.portfolio.service;

import com.portfolio.model.Stock;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class PortfolioTest {
    @Test void calculatesValueAfterBuyAndSell() {
        Portfolio p = new Portfolio();
        p.buy(new Stock("ABC", new BigDecimal("10.25")), new BigDecimal("4"));
        assertEquals(0, p.marketValue().compareTo(new BigDecimal("41.00")));
        p.sell("ABC", new BigDecimal("1"));
        assertEquals(0, p.marketValue().compareTo(new BigDecimal("30.75")));
    }
    @Test void rejectsOverselling() {
        Portfolio p = new Portfolio();
        p.buy(new Stock("ABC", BigDecimal.TEN), BigDecimal.ONE);
        assertThrows(IllegalArgumentException.class, () -> p.sell("ABC", new BigDecimal("2")));
        assertEquals(BigDecimal.ONE, p.positions().get("ABC"));
    }
    @Test void rejectsInvalidQuantitiesAndPrices() {
        Portfolio p = new Portfolio();
        assertThrows(IllegalArgumentException.class, () -> p.buy(new Stock("ABC", BigDecimal.ONE), BigDecimal.ZERO));
        assertThrows(IllegalArgumentException.class, () -> new Stock("ABC", new BigDecimal("-1")));
    }
    @Test void repricesExistingHolding() {
        Portfolio p = new Portfolio();
        p.buy(new Stock("ABC", new BigDecimal("10")), BigDecimal.ONE);
        p.updatePrice(new Stock("ABC", new BigDecimal("12")));
        assertEquals(0, p.marketValue().compareTo(new BigDecimal("12")));
    }
}
