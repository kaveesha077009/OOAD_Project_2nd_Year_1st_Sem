package com.Agro_Shop.agroshop.Services;

import com.Agro_Shop.agroshop.Entities.Order;
import com.Agro_Shop.agroshop.Entities.Payment;
import com.Agro_Shop.agroshop.Repositories.OrderRepository;
import com.Agro_Shop.agroshop.Repositories.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private OrderRepository orderRepository;

    public Payment processPayment(Payment payment) {
        Payment savedPayment = paymentRepository.save(payment);

        // If payment is completed, update order status to PROCESSING
        if ("COMPLETED".equalsIgnoreCase(payment.getStatus())) {
            Order order = orderRepository.findById(payment.getOrderId()).orElse(null);
            if (order != null) {
                order.setStatus("PROCESSING");
                orderRepository.save(order);
            }
        }
        return savedPayment;
    }

    public Payment getPaymentByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId).orElse(null);
    }

    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }
}