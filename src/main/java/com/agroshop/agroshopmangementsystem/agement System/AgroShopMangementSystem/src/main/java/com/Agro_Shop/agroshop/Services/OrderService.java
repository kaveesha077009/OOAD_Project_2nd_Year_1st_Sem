package com.Agro_Shop.agroshop.Services;

import com.Agro_Shop.agroshop.Entities.Order;
import com.Agro_Shop.agroshop.Entities.OrderItem;
import com.Agro_Shop.agroshop.Repositories.OrderItemRepository;
import com.Agro_Shop.agroshop.Repositories.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
public class OrderService {

    private static final Set<String> STATUSES =
            Set.of("PENDING", "PROCESSING", "COMPLETED", "CANCELLED");

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    // ---- employee manual sale ----
    public Order createOrder(Order order) {
        if (order.getOrderDate() == null) order.setOrderDate(LocalDateTime.now());
        if (order.getStatus() == null) order.setStatus("PENDING");
        if (order.getTotalAmount() == null) order.setTotalAmount(0.0);
        return orderRepository.save(order);
    }

    public OrderItem addOrderItem(OrderItem item) {
        BigDecimal unitPrice = (item.getUnitPrice() != null) ? item.getUnitPrice() : BigDecimal.ZERO;
        BigDecimal quantity  = (item.getQuantity()  != null) ? item.getQuantity()  : BigDecimal.ZERO;
        BigDecimal subtotal  = unitPrice.multiply(quantity);

        item.setSubtotal(subtotal);
        OrderItem savedItem = orderItemRepository.save(item);

        Order order = orderRepository.findById(item.getOrderId()).orElse(null);
        if (order != null) {
            double currentTotal = (order.getTotalAmount() != null) ? order.getTotalAmount() : 0.0;
            order.setTotalAmount(currentTotal + subtotal.doubleValue());
            orderRepository.save(order);
        }
        return savedItem;
    }

    // ---- customer order history ----
    public List<Order> getOrdersByCustomer(Long customerId) {
        return orderRepository.findByCustomerIdOrderByOrderDateDesc(customerId);
    }

    public List<OrderItem> getItemsByOrder(Long orderId) {
        return orderItemRepository.findByOrderId(orderId);
    }

    // ---- employee updates status ----
    public Order updateStatus(Long orderId, String status) {
        String s = (status == null) ? "" : status.trim().toUpperCase();
        if (!STATUSES.contains(s)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status must be one of " + STATUSES);
        }
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
        order.setStatus(s);
        return orderRepository.save(order);
    }
}