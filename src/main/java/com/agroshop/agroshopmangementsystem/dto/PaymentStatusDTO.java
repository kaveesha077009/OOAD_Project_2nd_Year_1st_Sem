package com.agroshop.agroshopmangementsystem.dto;

public class PaymentStatusDTO {
    private Long orderId;
    private String paymentMethod;
    private String status;

    public PaymentStatusDTO() {}

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}