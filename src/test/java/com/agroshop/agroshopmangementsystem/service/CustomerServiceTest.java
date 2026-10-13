package com.agroshop.agroshopmangementsystem.service;

import com.agroshop.agroshopmangementsystem.entity.Customer;
import com.agroshop.agroshopmangementsystem.repository.CustomerRepository;
import com.agroshop.agroshopmangementsystem.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomerServiceTest {

    private CustomerRepository customerRepository;
    private CustomerService service;

    @BeforeEach
    void setUp() {
        customerRepository = mock(CustomerRepository.class);

        service = new CustomerService();

        org.springframework.test.util.ReflectionTestUtils.setField(
                service, "customerRepository", customerRepository
        );
        org.springframework.test.util.ReflectionTestUtils.setField(
                service, "userRepository", mock(UserRepository.class)
        );
        org.springframework.test.util.ReflectionTestUtils.setField(
                service, "passwordEncoder", mock(PasswordEncoder.class)
        );
    }

    @Test
    void shouldFindCustomerById() {
        Customer customer = new Customer();
        customer.setId(1L);

        when(customerRepository.findById(1L))
                .thenReturn(Optional.of(customer));

        assertSame(customer, service.getCustomerById(1L));
    }

    @Test
    void shouldUpdateCustomerWithoutChangingAccountDetails() {
        Customer existing = new Customer();
        existing.setId(1L);
        existing.setUserId(10L);
        existing.setUsername("customer1");
        existing.setStatus(1);

        Customer details = new Customer();
        details.setFirstName("Nimal");
        details.setEmail("nimal@example.com");
        details.setUserId(99L);
        details.setUsername("changed");
        details.setStatus(0);

        when(customerRepository.findById(1L))
                .thenReturn(Optional.of(existing));
        when(customerRepository.save(existing))
                .thenReturn(existing);

        Customer result = service.updateCustomer(1L, details);

        assertEquals("Nimal", result.getFirstName());
        assertEquals("nimal@example.com", result.getEmail());

        assertEquals(10L, result.getUserId());
        assertEquals("customer1", result.getUsername());
        assertEquals(1, result.getStatus());

        verify(customerRepository).save(existing);
    }

    @Test
    void shouldRejectMissingCustomer() {
        when(customerRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.updateCustomer(99L, new Customer())
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(customerRepository, never()).save(any());
    }

    @Test
    void shouldRejectNullUpdateDetails() {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.updateCustomer(1L, null)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(customerRepository, never()).save(any());
    }

    @Test
    void shouldRejectRegistrationWithoutPassword() {
        Customer customer = new Customer();
        customer.setFirstName("Invalid");
        customer.setUsername("agro_invalid_test");
        customer.setEmail("invalid.test@example.com");

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.registerCustomer(customer)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(customerRepository, never()).save(any());
    }

}
