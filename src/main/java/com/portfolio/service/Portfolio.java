package com.portfolio.service;

import com.portfolio.model.Asset;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/** In-memory positions; prices supplied explicitly (no market-data feed). */
public final class Portfolio {
    private final Map<String, BigDecimal> quantities = new HashMap<>();
    private final Map<String, Asset> assets = new HashMap<>();

    public void buy(Asset asset, BigDecimal quantity) {
        validateQuantity(quantity);
        if (asset == null) throw new IllegalArgumentException("Asset is required");
        assets.put(asset.symbol(), asset);
        quantities.merge(asset.symbol(), quantity, BigDecimal::add);
    }

    public void sell(String symbol, BigDecimal quantity) {
        validateQuantity(quantity);
        if (symbol == null) throw new IllegalArgumentException("Symbol is required");
        String key = symbol.trim().toUpperCase(java.util.Locale.ROOT);
        BigDecimal held = quantities.getOrDefault(key, BigDecimal.ZERO);
        if (held.compareTo(quantity) < 0) throw new IllegalArgumentException("Insufficient holdings");
        BigDecimal remaining = held.subtract(quantity);
        if (remaining.signum() == 0) { quantities.remove(key); assets.remove(key); }
        else quantities.put(key, remaining);
    }

    public void updatePrice(Asset asset) {
        if (asset == null || !assets.containsKey(asset.symbol())) throw new IllegalArgumentException("Asset not held");
        assets.put(asset.symbol(), asset);
    }

    public BigDecimal marketValue() {
        return quantities.entrySet().stream()
                .map(e -> assets.get(e.getKey()).price().multiply(e.getValue()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Map<String, BigDecimal> positions() { return Collections.unmodifiableMap(new HashMap<>(quantities)); }

    private static void validateQuantity(BigDecimal quantity) {
        if (quantity == null || quantity.signum() <= 0) throw new IllegalArgumentException("Quantity must be positive");
    }
}
