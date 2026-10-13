package com.agroshop.agroshopmangementsystem.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double totalAmount;
    private String deliveryAddress;
    private String status;
    private String username;

    @ManyToOne
    @JoinColumn(name = "customer_id", referencedColumnName = "id", nullable = true)
    private Customer customer;

    public Order() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }

    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }
}