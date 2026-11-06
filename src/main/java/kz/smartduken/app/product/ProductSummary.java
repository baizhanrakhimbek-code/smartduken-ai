package kz.smartduken.app.product;

import java.math.BigDecimal;

/** Whole-store totals, independent of the product list filters. */
public record ProductSummary(long productCount, long lowStockCount,
                             long outOfStockCount, BigDecimal totalValue) {}
