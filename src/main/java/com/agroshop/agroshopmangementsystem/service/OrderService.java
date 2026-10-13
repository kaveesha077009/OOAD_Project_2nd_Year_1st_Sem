package com.agroshop.agroshopmangementsystem.service;

import com.agroshop.agroshopmangementsystem.entity.Customer;
import com.agroshop.agroshopmangementsystem.entity.Order;
import com.agroshop.agroshopmangementsystem.repository.CustomerRepository;
import com.agroshop.agroshopmangementsystem.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Transactional
    public Order placeOrderFromMap(Map<String, Object> payload) {
        Order order = new Order();

        String username = payload.containsKey("username") && payload.get("username") != null
                ? payload.get("username").toString() : "Guest";
        String deliveryAddress = payload.containsKey("deliveryAddress") && payload.get("deliveryAddress") != null
                ? payload.get("deliveryAddress").toString() : "";
        String status = payload.containsKey("status") && payload.get("status") != null
                ? payload.get("status").toString() : "PENDING";

        Double totalAmount = 0.0;
        if (payload.containsKey("totalAmount") && payload.get("totalAmount") != null) {
            totalAmount = Double.parseDouble(payload.get("totalAmount").toString());
        }

        order.setUsername(username);
        order.setDeliveryAddress(deliveryAddress);
        order.setStatus(status);
        order.setTotalAmount(totalAmount);


        // Find the customer using their registered username
        if (username.isBlank() || username.equals("Guest")) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST,
                    "A registered customer is required");
        }

        Customer customerToAttach = customerRepository.findAll()
                .stream()
                .filter(c -> username.equals(c.getUsername()))
                .findFirst()
                .orElseThrow(() ->
                        new org.springframework.web.server.ResponseStatusException(
                                org.springframework.http.HttpStatus.NOT_FOUND,
                                "Customer not found"));

        order.setCustomer(customerToAttach);
        return orderRepository.save(order);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public List<Order> getOrdersByUsername(String username) {
        return orderRepository.findByUsername(username);
    }
}