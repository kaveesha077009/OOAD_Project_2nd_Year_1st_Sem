package com.agroshop.demo.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public record OrderRequest(
        @NotNull(message = "Choose a payment method") String paymentMethod,
        @NotEmpty(message = "Add at least one item") @Valid List<Line> items) {

    public record Line(
            @NotNull(message = "Product is required") Long productId,
            @NotNull(message = "Quantity is required") BigDecimal quantity) {
    }
}
