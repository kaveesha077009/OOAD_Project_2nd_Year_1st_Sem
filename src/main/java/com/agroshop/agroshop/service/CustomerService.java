package com.agroshop.agroshop.service;

import com.agroshop.agroshop.Entity.Customer;
import com.agroshop.agroshop.Entity.User;
import com.agroshop.agroshop.repository.CustomerRepository;
import com.agroshop.agroshop.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    public Customer getCustomerById(Long id) {
        return customerRepository.findById(id).orElse(null);
    }

    @Transactional
    public void saveCustomerWithUser(Customer customer, String username, String rawPassword) {
        String encodedPassword = passwordEncoder.encode(rawPassword);

        User user = new User();
        user.setUsername(username);
        user.setPassword(encodedPassword);
        user.setEmail(customer.getEmail());
        user.setRole("CUSTOMER");
        user.setStatus(true);

        User savedUser = userRepository.save(user);

        customer.setUser(savedUser);
        customer.setRole("CUSTOMER");
        customer.setStatus(true);

        customerRepository.save(customer);
    }

    public void updateCustomer(Customer customer) {
        customerRepository.save(customer);
    }

    public void deleteCustomer(Long id) {
        customerRepository.deleteById(id);
    }
}