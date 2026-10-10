package com.Agro_Shop.agroshop.Services;

import com.Agro_Shop.agroshop.Entities.Order;
import com.Agro_Shop.agroshop.Entities.Payment;
import com.Agro_Shop.agroshop.Repositories.OrderRepository;
import com.Agro_Shop.agroshop.Repositories.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private OrderRepository orderRepository;

    public Payment processPayment(Payment payment) {
        Payment savedPayment = paymentRepository.save(payment);

        if ("COMPLETED".equalsIgnoreCase(payment.getStatus())) {
            markOrderProcessing(payment.getOrderId());
        }
        return savedPayment;
    }
// To confirm cash received by the employee(bank transferr)
    public Payment confirmPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found"));

        payment.setStatus("COMPLETED");
        Payment saved = paymentRepository.save(payment);
        markOrderProcessing(payment.getOrderId());
        return saved;
    }
// reject payment
    public Payment failPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found"));
        payment.setStatus("FAILED");
        return paymentRepository.save(payment);
    }

    public Payment getPaymentByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId).orElse(null);
    }

    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    private void markOrderProcessing(Long orderId) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order != null) {
            order.setStatus("PROCESSING");
            orderRepository.save(order);
        }
    }
}