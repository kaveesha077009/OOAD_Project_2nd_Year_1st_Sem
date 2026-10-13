package com.agroshop.agroshopmangementsystem.service;

import com.agroshop.agroshopmangementsystem.entity.Customer;
import com.agroshop.agroshopmangementsystem.entity.User;
import com.agroshop.agroshopmangementsystem.repository.CustomerRepository;
import com.agroshop.agroshopmangementsystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private PasswordEncoder passwordEncoder;

    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }


    public Customer getCustomerById(Long id) {
        return customerRepository.findById(id).orElse(null);
    }

    @Transactional
    public Customer registerCustomer(Customer customer) {
        String uname = (customer.getUsername() != null && !customer.getUsername().isEmpty())
                ? customer.getUsername()
                : customer.getFirstName().toLowerCase();

        // 1. Create User
        User user = new User();
        user.setUsername(uname);
        user.setEmail(customer.getEmail());
        user.setRole("CUSTOMER");
        user.setStatus(1);

        if (customer.getPassword() != null && !customer.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(customer.getPassword()));
        } else {
            user.setPassword(passwordEncoder.encode("default123"));
        }

        User savedUser = userRepository.save(user);

        // 2. Save Customer with linked User ID and Username
        customer.setUserId(savedUser.getId());
        customer.setUsername(uname);
        customer.setStatus(1);

        return customerRepository.save(customer);
    }


    @Transactional
    public Customer updateCustomer(Long id, Customer details) {
        if (details == null) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST,
                    "Customer details are required");
        }

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() ->
                        new org.springframework.web.server.ResponseStatusException(
                                org.springframework.http.HttpStatus.NOT_FOUND,
                                "Customer not found"));

        customer.setFirstName(details.getFirstName());
        customer.setLastName(details.getLastName());
        customer.setEmail(details.getEmail());
        customer.setPhoneNum(details.getPhoneNum());
        customer.setAddress(details.getAddress());

        return customerRepository.save(customer);
    }

    public void deleteCustomer(Long id) {
        customerRepository.deleteById(id);
    }
}