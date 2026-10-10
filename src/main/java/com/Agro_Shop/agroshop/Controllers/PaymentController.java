package com.Agro_Shop.agroshop.Controllers;

import com.Agro_Shop.agroshop.Entities.Payment;
import com.Agro_Shop.agroshop.Services.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping
    public Payment processPayment(@RequestBody Payment payment) {
        return paymentService.processPayment(payment);
    }

    @GetMapping
    public List<Payment> getAllPayments() {
        return paymentService.getAllPayments();
    }

    @GetMapping("/order/{orderId}")
    public Payment getPaymentByOrder(@PathVariable Long orderId) {
        return paymentService.getPaymentByOrderId(orderId);
    }

    @PatchMapping("/{paymentId}/confirm")
    public Payment confirmPayment(@PathVariable Long paymentId) {
        return paymentService.confirmPayment(paymentId);
    }

    @PatchMapping("/{paymentId}/fail")
    public Payment failPayment(@PathVariable Long paymentId) {
        return paymentService.failPayment(paymentId);
    }
}