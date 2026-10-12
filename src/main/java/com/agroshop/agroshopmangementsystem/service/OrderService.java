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

        // Fetch or create a Customer record to ensure customer_id is never null
        Customer customerToAttach = null;
        List<Customer> customers = customerRepository.findAll();

        if (!customers.isEmpty()) {
            Optional<Customer> matchedCustomer = customers.stream()
                    .filter(c -> (c.getEmail() != null && c.getEmail().equalsIgnoreCase(username)) ||
                            (c.getFirstName() != null && c.getFirstName().equalsIgnoreCase(username)))
                    .findFirst();

            customerToAttach = matchedCustomer.orElse(customers.get(0));
        } else {
            // Create a default customer record if database table is completely empty
            Customer defaultCustomer = new Customer();
            defaultCustomer.setFirstName("Default");
            defaultCustomer.setLastName("Customer");
            defaultCustomer.setEmail("default@agroshop.com");
            defaultCustomer.setPhoneNum("0000000000");
            defaultCustomer.setAddress(deliveryAddress);
            customerToAttach = customerRepository.saveAndFlush(defaultCustomer);
        }

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