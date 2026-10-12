package com.agroshop.demo.controller;

import com.agroshop.demo.dto.OrderRequest;
import com.agroshop.demo.dto.OrderResponse;
import com.agroshop.demo.entity.OrderStatus;
import com.agroshop.demo.service.OrderService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST endpoints for placing, viewing and managing orders.
 * <p>
 * Customer endpoints: {@code POST /api/orders}, {@code GET /api/orders/my}.<br>
 * Staff endpoints: everything else.
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /** Customer: place an online order from the cart. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse placeOnlineOrder(@Valid @RequestBody OrderRequest request,
                                          Authentication auth) {
        log.info("Online order requested by user '{}'", auth.getName());
        return orderService.placeOnlineOrder(auth.getName(), request);
    }

    /** Customer: list the logged-in customer's own orders. */
    @GetMapping("/my")
    public List<OrderResponse> getMyOrders(Authentication auth) {
        return orderService.myOrders(auth.getName());
    }

    /** Staff: record a counter (in-shop) sale. */
    @PostMapping("/sale")
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse createCounterSale(@Valid @RequestBody OrderRequest request) {
        log.info("Counter sale requested");
        return orderService.counterSale(request);
    }

    /** Staff: list all orders. */
    @GetMapping
    public List<OrderResponse> getAllOrders() {
        return orderService.allOrders();
    }

    /** Staff: update the status of an order. */
    @PutMapping("/{id}/status")
    public OrderResponse updateStatus(@PathVariable("id") Long id,
                                      @RequestParam("status") OrderStatus status) {
        log.info("Updating order {} status to {}", id, status);
        return orderService.updateStatus(id, status);
    }

    /** Staff: confirm that payment for an order has been received. */
    @PutMapping("/{id}/payment/confirm")
    public OrderResponse confirmPayment(@PathVariable("id") Long id) {
        log.info("Confirming payment for order {}", id);
        return orderService.confirmPayment(id);
    }
}
