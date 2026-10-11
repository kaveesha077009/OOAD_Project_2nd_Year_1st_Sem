
package com.agroshop.agroshop.Entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "purchase_orders")
public class PurchaseOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Supplier associated with the order
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @Column(nullable = false)
    private LocalDate orderDate;

    // Existing fields kept for compatibility
    @Column(nullable = false)
    private String product;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private String status = "Pending";

    // One order can contain multiple items
    @OneToMany(
            mappedBy = "purchaseOrder",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<PurchaseOrderItem> items =
            new ArrayList<>();

    public PurchaseOrder() {
    }

    // Add an item to this purchase order
    public void addItem(PurchaseOrderItem item) {

        item.setPurchaseOrder(this);
        items.add(item);
    }

    // Remove an item from this purchase order
    public void removeItem(PurchaseOrderItem item) {

        items.remove(item);
        item.setPurchaseOrder(null);
    }

    // Calculate the total order amount
    @Transient
    public BigDecimal getTotalAmount() {

        // Use the existing fields for older orders
        if (items.isEmpty()) {

            if (quantity == null || price == null) {
                return BigDecimal.ZERO;
            }

            return price.multiply(
                    BigDecimal.valueOf(quantity)
            );
        }

        // Calculate the total of all items
        BigDecimal total = BigDecimal.ZERO;

        for (PurchaseOrderItem item : items) {
            total = total.add(item.getTotalPrice());
        }

        return total;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Supplier getSupplier() {
        return supplier;
    }

    public void setSupplier(Supplier supplier) {
        this.supplier = supplier;
    }

    public LocalDate getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDate orderDate) {
        this.orderDate = orderDate;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<PurchaseOrderItem> getItems() {
        return items;
    }

    public void setItems(List<PurchaseOrderItem> newItems) {

        for (PurchaseOrderItem item :
                new ArrayList<>(items)) {
            removeItem(item);
        }

        if (newItems != null) {
            for (PurchaseOrderItem item : newItems) {
                addItem(item);
            }
        }
    }
}
