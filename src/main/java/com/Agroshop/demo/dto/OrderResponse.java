package com.agroshop.demo.dto;

import com.agroshop.demo.entity.CustomerOrder;
import com.agroshop.demo.entity.Payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long id, LocalDateTime orderDate, String customerName, BigDecimal total, String status,
        String paymentMethod, String paymentStatus, List<Line> items) {

    public record Line(String productName, String unit, BigDecimal quantity, BigDecimal unitPrice, BigDecimal subtotal) {
    }

    public static OrderResponse of(CustomerOrder o, Payment p) {
        return new OrderResponse(o.getId(), o.getOrderDate(), o.getCustomer().getFullName(), o.getTotalAmount(),
                o.getStatus().name(), p == null ? null : p.getPaymentMethod(), p == null ? null : p.getStatus().name(),
                o.getItems().stream().map(i -> new Line(i.getProduct().getName(), i.getProduct().getUnit(),
                        i.getQuantity(), i.getUnitPrice(), i.getSubtotal())).toList());
    }
}
