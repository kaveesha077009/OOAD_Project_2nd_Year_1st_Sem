
package com.agroshop.agroshop.service;

import com.agroshop.agroshop.Entity.Supplier;
import com.agroshop.agroshop.repository.SupplierRepository;
import com.agroshop.agroshop.repository.PurchaseOrderRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SupplierServiceTest {

    private SupplierRepository supplierRepository;
    private PurchaseOrderRepository orderRepository;
    private SupplierService service;

    private Supplier supplier;

    // Prepare test data before each test
    @BeforeEach
    void setUp() {

        supplierRepository = mock(SupplierRepository.class);
        orderRepository = mock(PurchaseOrderRepository.class);

        service = new SupplierService(
                supplierRepository,
                orderRepository
        );

        supplier = new Supplier(
                "Agro Lanka",
                "0771234567",
                "agro@example.com",
                "Colombo"
        );

        supplier.setSupplierId(1L);
    }

    // Test 1: Add a valid supplier
    @Test
    void testAddSupplier() {

        when(supplierRepository.save(any(Supplier.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Supplier result = service.addSupplier(supplier);

        assertEquals("Agro Lanka", result.getName());
        assertEquals("0771234567", result.getPhone());
        assertNull(result.getSupplierId());

        verify(supplierRepository).save(supplier);
    }

    // Test 2: Retrieve all suppliers
    @Test
    void testGetAllSuppliers() {

        when(supplierRepository.findAll())
                .thenReturn(List.of(supplier));

        List<Supplier> result = service.getAllSuppliers();

        assertEquals(1, result.size());
        assertEquals("Agro Lanka", result.get(0).getName());

        verify(supplierRepository).findAll();
    }

    // Test 3: Find supplier by ID
    @Test
    void testGetSupplierById() {

        when(supplierRepository.findById(1L))
                .thenReturn(Optional.of(supplier));

        Optional<Supplier> result =
                service.getSupplierById(1L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getSupplierId());

        verify(supplierRepository).findById(1L);
    }

    // Test 4: Find nonexistent supplier
    @Test
    void testGetSupplierNotFound() {

        when(supplierRepository.findById(99L))
                .thenReturn(Optional.empty());

        Optional<Supplier> result =
                service.getSupplierById(99L);

        assertTrue(result.isEmpty());
    }

    // Test 5: Update supplier successfully
    @Test
    void testUpdateSupplier() {

        supplier.setName("Updated Agro Lanka");
        supplier.setAddress("Kandy");

        when(supplierRepository.existsById(1L))
                .thenReturn(true);

        when(supplierRepository.save(supplier))
                .thenReturn(supplier);

        Supplier result = service.updateSupplier(supplier);

        assertEquals("Updated Agro Lanka", result.getName());
        assertEquals("Kandy", result.getAddress());

        verify(supplierRepository).save(supplier);
    }

    // Test 6: Reject update for nonexistent supplier
    @Test
    void testUpdateSupplierNotFound() {

        when(supplierRepository.existsById(1L))
                .thenReturn(false);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.updateSupplier(supplier)
        );

        assertEquals(
                HttpStatus.NOT_FOUND,
                exception.getStatusCode()
        );

        verify(supplierRepository, never())
                .save(any(Supplier.class));
    }

    // Test 7: Reject empty supplier name
    @Test
    void testEmptySupplierName() {

        supplier.setName("");

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.addSupplier(supplier)
        );

        assertEquals(
                HttpStatus.BAD_REQUEST,
                exception.getStatusCode()
        );

        verify(supplierRepository, never())
                .save(any(Supplier.class));
    }

    // Test 8: Reject invalid phone number
    @Test
    void testInvalidPhoneNumber() {

        supplier.setPhone("12345");

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.addSupplier(supplier)
        );

        assertEquals(
                HttpStatus.BAD_REQUEST,
                exception.getStatusCode()
        );

        verify(supplierRepository, never())
                .save(any(Supplier.class));
    }

    // Test 9: Reject invalid email address
    @Test
    void testInvalidEmail() {

        supplier.setEmail("invalid-email");

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.addSupplier(supplier)
        );

        assertEquals(
                HttpStatus.BAD_REQUEST,
                exception.getStatusCode()
        );

        verify(supplierRepository, never())
                .save(any(Supplier.class));
    }

    // Test 10: Allow an empty optional email
    @Test
    void testEmptyEmailAllowed() {

        supplier.setEmail("");

        when(supplierRepository.save(any(Supplier.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Supplier result = service.addSupplier(supplier);

        assertEquals("", result.getEmail());

        verify(supplierRepository).save(supplier);
    }

    // Test 11: Reject duplicate phone number
    @Test
    void testDuplicatePhoneNumber() {

        Supplier existing = new Supplier(
                "Another Supplier",
                "0771234567",
                "another@example.com",
                "Galle"
        );

        existing.setSupplierId(2L);

        when(supplierRepository.findByPhone("0771234567"))
                .thenReturn(Optional.of(existing));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.addSupplier(supplier)
        );

        assertEquals(
                HttpStatus.CONFLICT,
                exception.getStatusCode()
        );

        verify(supplierRepository, never())
                .save(any(Supplier.class));
    }

    // Test 12: Delete supplier successfully
    @Test
    void testDeleteSupplier() {

        when(supplierRepository.existsById(1L))
                .thenReturn(true);

        when(orderRepository.existsBySupplier_SupplierId(1L))
                .thenReturn(false);

        service.deleteSupplier(1L);

        verify(supplierRepository).deleteById(1L);
    }

    // Test 13: Reject deleting nonexistent supplier
    @Test
    void testDeleteSupplierNotFound() {

        when(supplierRepository.existsById(99L))
                .thenReturn(false);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.deleteSupplier(99L)
        );

        assertEquals(
                HttpStatus.NOT_FOUND,
                exception.getStatusCode()
        );

        verify(supplierRepository, never())
                .deleteById(anyLong());
    }

    // Test 14: Prevent deleting supplier with orders
    @Test
    void testDeleteSupplierWithOrders() {

        when(supplierRepository.existsById(1L))
                .thenReturn(true);

        when(orderRepository.existsBySupplier_SupplierId(1L))
                .thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.deleteSupplier(1L)
        );

        assertEquals(
                HttpStatus.CONFLICT,
                exception.getStatusCode()
        );

        verify(supplierRepository, never())
                .deleteById(anyLong());
    }

    // Test 15: Reject null supplier information
    @Test
    void testNullSupplier() {

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.addSupplier(null)
        );

        assertEquals(
                HttpStatus.BAD_REQUEST,
                exception.getStatusCode()
        );

        verify(supplierRepository, never())
                .save(any(Supplier.class));
    }
}
