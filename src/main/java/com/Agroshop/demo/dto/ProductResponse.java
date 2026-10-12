package com.agroshop.demo.dto;

import com.agroshop.demo.entity.Inventory;
import com.agroshop.demo.entity.Product;

import java.math.BigDecimal;

public record ProductResponse(
        Long id, String name, String description, BigDecimal price, String unit, String imageUrl,
        Long categoryId, String categoryName,
        BigDecimal quantity, BigDecimal minimumStock, boolean lowStock) {

    public static ProductResponse of(Product p, Inventory i) {
        BigDecimal qty = i == null ? BigDecimal.ZERO : i.getQuantity();
        BigDecimal min = i == null ? BigDecimal.ZERO : i.getMinimumStock();
        return new ProductResponse(p.getId(), p.getName(), p.getDescription(), p.getPrice(), p.getUnit(),
                p.getImageUrl(), p.getCategory().getId(), p.getCategory().getName(),
                qty, min, i != null && i.isLow());
    }
}
