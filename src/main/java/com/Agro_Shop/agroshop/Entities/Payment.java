package com.Agro_Shop.agroshop.Entities;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false, unique = true)
    private Long orderId;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(name = "payment_method", nullable = false)
    private String paymentMethod;

    @Column(name = "payment_date", insertable = false, updatable = false)
    private LocalDateTime paymentDate;

    private String status = "PENDING";

    public Payment() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public LocalDateTime getPaymentDate() { return paymentDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}