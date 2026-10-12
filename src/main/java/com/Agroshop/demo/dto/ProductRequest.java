package com.agroshop.demo.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProductRequest(
        @NotBlank(message = "Product name is required") @Size(max = 100) String name,
        String description,
        @NotNull(message = "Price is required") @DecimalMin(value = "0.00", message = "Price cannot be negative") BigDecimal price,
        @NotBlank(message = "Unit is required (e.g. kg, bag, piece)") @Size(max = 20) String unit,
        @Size(max = 500) String imageUrl,
        @NotNull(message = "Category is required") Long categoryId,
        @DecimalMin(value = "0.00", message = "Quantity cannot be negative") BigDecimal quantity,
        @DecimalMin(value = "0.00", message = "Minimum stock cannot be negative") BigDecimal minimumStock) {
}
