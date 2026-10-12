package com.agroshop.agroshopmangementsystem.controller;

import com.agroshop.agroshopmangementsystem.entity.Order;
import com.agroshop.agroshopmangementsystem.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/place")
    public ResponseEntity<?> placeOrder(@RequestBody Map<String, Object> payload) {
        try {
            Order savedOrder = orderService.placeOrderFromMap(payload);
            return ResponseEntity.ok(savedOrder);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @GetMapping("/customer/{username}")
    public ResponseEntity<List<Order>> getCustomerOrders(@PathVariable String username) {
        return ResponseEntity.ok(orderService.getOrdersByUsername(username));
    }
}