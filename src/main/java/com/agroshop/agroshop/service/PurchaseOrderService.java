
package com.agroshop.agroshop.service;

import com.agroshop.agroshop.Entity.PurchaseOrder;
import com.agroshop.agroshop.Entity.PurchaseOrderItem;
import com.agroshop.agroshop.Entity.Supplier;

import com.agroshop.agroshop.repository.PurchaseOrderRepository;
import com.agroshop.agroshop.repository.SupplierRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class PurchaseOrderService {

    private final PurchaseOrderRepository orders;
    private final SupplierRepository suppliers;

    public PurchaseOrderService(
            PurchaseOrderRepository orders,
            SupplierRepository suppliers) {

        this.orders = orders;
        this.suppliers = suppliers;
    }

    // Get all purchase orders
    public List<PurchaseOrder> findAll() {
        return orders.findAll();
    }

    // Find a purchase order by ID
    public PurchaseOrder findById(Long id) {

        return orders.findById(id).orElseThrow(() ->
                new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Purchase order not found"
                )
        );
    }

    // Validate product information
    private void validateItem(
            String product,
            Integer quantity,
            BigDecimal price) {

        if (product == null || product.isBlank() ||
                quantity == null || quantity < 1 ||
                price == null || price.signum() < 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid purchase item"
            );
        }
    }

    // Find the supplier
    private Supplier findSupplier(Long supplierId) {

        if (supplierId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Supplier ID is required"
            );
        }

        return suppliers.findById(supplierId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Supplier not found"
                        )
                );
    }

    // Create a single-product purchase order
    @Transactional
    public PurchaseOrder create(
            Long supplierId,
            LocalDate date,
            String product,
            Integer quantity,
            BigDecimal price) {

        if (date == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Order date is required"
            );
        }

        validateItem(product, quantity, price);

        Supplier supplier = findSupplier(supplierId);

        PurchaseOrder order = new PurchaseOrder();

        order.setSupplier(supplier);
        order.setOrderDate(date);
        order.setProduct(product.trim());
        order.setQuantity(quantity);
        order.setPrice(price);
        order.setStatus("Pending");

        return orders.save(order);
    }

    // Create a purchase order with multiple products
    @Transactional
    public PurchaseOrder createMultiple(
            Long supplierId,
            LocalDate date,
            List<PurchaseOrderItem> items) {

        // Validate order date
        if (date == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Order date is required"
            );
        }

        // An order must contain at least one product
        if (items == null || items.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Purchase order must contain products"
            );
        }

        // Validate every item before saving
        for (PurchaseOrderItem item : items) {

            if (item == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Purchase item cannot be null"
                );
            }

            validateItem(
                    item.getProduct(),
                    item.getQuantity(),
                    item.getUnitPrice()
            );
        }

        Supplier supplier = findSupplier(supplierId);

        PurchaseOrder order = new PurchaseOrder();

        order.setSupplier(supplier);
        order.setOrderDate(date);
        order.setStatus("Pending");

        // Preserve legacy required database columns
        PurchaseOrderItem first = items.get(0);

        order.setProduct(first.getProduct().trim());
        order.setQuantity(first.getQuantity());
        order.setPrice(first.getUnitPrice());

        // Add all products to the order
        for (PurchaseOrderItem item : items) {

            PurchaseOrderItem newItem =
                    new PurchaseOrderItem(
                            item.getProduct().trim(),
                            item.getQuantity(),
                            item.getUnitPrice()
                    );

            order.addItem(newItem);
        }

        // CascadeType.ALL saves associated items
        return orders.save(order);
    }

    // Mark a purchase order as received
    @Transactional
    public PurchaseOrder receive(Long id) {

        PurchaseOrder order = findById(id);

        if (!"Pending".equals(order.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Order already received"
            );
        }

        order.setStatus("Received");

        // Inventory integration will be added later
        return orders.save(order);
    }
}
