package com.Agro_Shop.agroshop.Services;

import com.Agro_Shop.agroshop.Entities.Order;
import com.Agro_Shop.agroshop.Entities.OrderItem;
import com.Agro_Shop.agroshop.Repositories.OrderItemRepository;
import com.Agro_Shop.agroshop.Repositories.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    public Order createOrder(Order order) {
        return orderRepository.save(order);
    }

    public OrderItem addOrderItem(OrderItem item) {
        // 1. Get Unit Price as double
        double unitPrice = (item.getUnitPrice() != null) ? item.getUnitPrice().doubleValue() : 0.0;

        // 2. Convert quantity (BigDecimal) to double safely
        double quantity = (item.getQuantity() != null) ? item.getQuantity().doubleValue() : 0.0;

        // 3. Calculate Subtotal using double arithmetic
        double subtotal = unitPrice * quantity;

        // 4. Save OrderItem
        OrderItem savedItem = orderItemRepository.save(item);

        // 5. Update Order's total amount
        Order order = orderRepository.findById(item.getOrderId()).orElse(null);
        if (order != null) {
            double currentTotal = (order.getTotalAmount() != null) ? order.getTotalAmount() : 0.0;
            double newTotal = currentTotal + subtotal;

            order.setTotalAmount(newTotal);
            orderRepository.save(order);
        }

        return savedItem;
    }
}