package com.agroshop.agroshopmangementsystem.controller;

import com.agroshop.agroshopmangementsystem.dto.PurchaseOrderRequest;
import com.agroshop.agroshopmangementsystem.entity.PurchaseItem;
import com.agroshop.agroshopmangementsystem.entity.PurchaseOrder;
import com.agroshop.agroshopmangementsystem.service.PurchaseOrderService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/purchase-orders")
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;

    public PurchaseOrderController(PurchaseOrderService purchaseOrderService) {
        this.purchaseOrderService = purchaseOrderService;
    }

    // View all purchase orders
    @GetMapping
    public List<PurchaseOrder> getAllPurchaseOrders() {
        return purchaseOrderService.getAllPurchaseOrders();
    }

    // View a purchase order by ID
    @GetMapping("/{id}")
    public PurchaseOrder getPurchaseOrderById(@PathVariable Long id) {
        return purchaseOrderService.getPurchaseOrderById(id);
    }

    // View products in a purchase order
    @GetMapping("/{id}/items")
    public List<PurchaseItem> getOrderItems(@PathVariable Long id) {
        return purchaseOrderService.getItemsByOrderId(id);
    }

    // Create a purchase order with multiple products
    @PostMapping
    public ResponseEntity<PurchaseOrder> createPurchaseOrder(
            @RequestBody PurchaseOrderRequest request) {

        PurchaseOrder savedOrder =
                purchaseOrderService.createPurchaseOrder(request);

        return ResponseEntity.status(201).body(savedOrder);
    }

    // Mark a purchase order as received
    @PatchMapping("/{id}/receive")
    public PurchaseOrder receivePurchaseOrder(@PathVariable Long id) {
        return purchaseOrderService.receivePurchaseOrder(id);
    }
}
