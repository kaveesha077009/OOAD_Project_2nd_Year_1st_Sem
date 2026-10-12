package com.agroshop.demo.controller;

import com.agroshop.demo.dto.OrderRequest;
import com.agroshop.demo.dto.OrderResponse;
import com.agroshop.demo.entity.OrderStatus;
import com.agroshop.demo.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /** Customer: place an order from the cart. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse place(@Valid @RequestBody OrderRequest request, Authentication auth) {
        return orderService.placeOnlineOrder(auth.getName(), request);
    }

    /** Customer: own orders. */
    @GetMapping("/my")
    public List<OrderResponse> mine(Authentication auth) {
        return orderService.myOrders(auth.getName());
    }

    /** Staff: counter sale. */
    @PostMapping("/sale")
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse sale(@Valid @RequestBody OrderRequest request) {
        return orderService.counterSale(request);
    }

    /** Staff: all orders. */
    @GetMapping
    public List<OrderResponse> all() {
        return orderService.allOrders();
    }

    @PutMapping("/{id}/status")
    public OrderResponse status(@PathVariable Long id, @RequestParam OrderStatus status) {
        return orderService.updateStatus(id, status);
    }

    @PutMapping("/{id}/payment/confirm")
    public OrderResponse confirmPayment(@PathVariable Long id) {
        return orderService.confirmPayment(id);
    }
}
