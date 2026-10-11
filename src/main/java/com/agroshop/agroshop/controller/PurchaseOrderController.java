
package com.agroshop.agroshop.controller;

import com.agroshop.agroshop.Entity.PurchaseOrder;
import com.agroshop.agroshop.Entity.PurchaseOrderItem;
import com.agroshop.agroshop.service.PurchaseOrderService;

import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/purchase-orders")
public class PurchaseOrderController {

    private final PurchaseOrderService service;

    public PurchaseOrderController(
            PurchaseOrderService service) {
        this.service = service;
    }

    // Request format for a single-product order
    public record CreateOrderRequest(
            Long supplierId,
            LocalDate date,
            String product,
            Integer quantity,
            BigDecimal price
    ) {}

    // Request format for an order item
    public record OrderItemRequest(
            String product,
            Integer quantity,
            BigDecimal unitPrice
    ) {}

    // Request format for multiple products
    public record CreateMultipleOrderRequest(
            Long supplierId,
            LocalDate date,
            List<OrderItemRequest> items
    ) {}

    // Get all purchase orders
    @GetMapping
    public List<PurchaseOrder> getAllOrders() {
        return service.findAll();
    }

    // Get a purchase order by ID
    @GetMapping("/{id}")
    public PurchaseOrder getOrderById(
            @PathVariable Long id) {
        return service.findById(id);
    }

    // Create a single-product order
    @PostMapping
    public PurchaseOrder createOrder(
            @RequestBody CreateOrderRequest request) {

        return service.create(
                request.supplierId(),
                request.date(),
                request.product(),
                request.quantity(),
                request.price()
        );
    }

    // Create an order with multiple products
    @PostMapping("/multiple")
    public PurchaseOrder createMultipleOrder(
            @RequestBody CreateMultipleOrderRequest request) {

        // Convert request data into purchase items
        List<PurchaseOrderItem> items =
                request.items() == null
                        ? null
                        : request.items().stream()
                        .map(item -> item == null
                                        ? null
                                        : new PurchaseOrderItem(
                                        item.product(),
                                        item.quantity(),
                                        item.unitPrice()
                                )
                        )
                        .toList();

        return service.createMultiple(
                request.supplierId(),
                request.date(),
                items
        );
    }

    // Mark an order as received
    @PatchMapping("/{id}/receive")
    public PurchaseOrder receiveOrder(
            @PathVariable Long id) {
        return service.receive(id);
    }
}
