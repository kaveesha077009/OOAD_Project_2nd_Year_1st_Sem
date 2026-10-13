package com.agroshop.agroshopmangementsystem.service;

import com.agroshop.agroshopmangementsystem.entity.Payment;
import com.agroshop.agroshopmangementsystem.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    public Payment processPayment(Payment payment) {
        payment.setPaymentDate(LocalDateTime.now());
        payment.setStatus("PENDING");
        return paymentRepository.save(payment);
    }
}