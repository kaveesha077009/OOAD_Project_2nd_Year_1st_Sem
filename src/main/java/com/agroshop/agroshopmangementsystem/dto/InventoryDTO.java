package com.agroshop.agroshopmangementsystem.dto;

public class InventoryDTO {
    private Long productId;
    private Integer quantity;
    private Integer minimumStock;

    public InventoryDTO() {}

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public Integer getMinimumStock() { return minimumStock; }
    public void setMinimumStock(Integer minimumStock) { this.minimumStock = minimumStock; }
}