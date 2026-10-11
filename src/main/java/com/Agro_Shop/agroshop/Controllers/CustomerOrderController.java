package com.Agro_Shop.agroshop.Controllers;

import com.Agro_Shop.agroshop.Dto.CheckoutRequest;
import com.Agro_Shop.agroshop.Dto.CheckoutResponse;
import com.Agro_Shop.agroshop.Entities.Order;
import com.Agro_Shop.agroshop.Entities.OrderItem;
import com.Agro_Shop.agroshop.Services.CheckoutService;
import com.Agro_Shop.agroshop.Services.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class CustomerOrderController {

    @Autowired
    private CheckoutService checkoutService;

    @Autowired
    private OrderService orderService;

    @PostMapping("/checkout")
    public CheckoutResponse checkout(@RequestBody CheckoutRequest request) {
        return checkoutService.placeOrder(request.customerId(), request.paymentMethod());
    }

    @GetMapping("/customer/{customerId}")
    public List<Order> myOrders(@PathVariable Long customerId) {
        return orderService.getOrdersByCustomer(customerId);
    }

    @GetMapping("/{orderId}/items")
    public List<OrderItem> orderItems(@PathVariable Long orderId) {
        return orderService.getItemsByOrder(orderId);
    }

    @PatchMapping("/{orderId}/status")
    public Order updateStatus(@PathVariable Long orderId, @RequestParam String status) {
        return orderService.updateStatus(orderId, status);
    }
}
