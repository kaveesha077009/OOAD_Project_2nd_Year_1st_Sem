package com.agroshop.agroshop.controller;

import com.agroshop.agroshop.Entity.PurchaseOrder;
import com.agroshop.agroshop.service.PurchaseOrderService;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/purchase-orders")
public class PurchaseOrderController {
    private final PurchaseOrderService service;
    public PurchaseOrderController(PurchaseOrderService service) { this.service = service; }
    public record CreateOrderRequest(Long supplierId, LocalDate date, String product, Integer quantity, BigDecimal price) {}
    @GetMapping public List<PurchaseOrder> all() { return service.findAll(); }
    @GetMapping("/{id}") public PurchaseOrder one(@PathVariable Long id) { return service.findById(id); }
    @PostMapping public PurchaseOrder create(@RequestBody CreateOrderRequest request) {
        return service.create(request.supplierId(), request.date(), request.product(), request.quantity(), request.price());
    }
    @PatchMapping("/{id}/receive") public PurchaseOrder receive(@PathVariable Long id) { return service.receive(id); }
}
