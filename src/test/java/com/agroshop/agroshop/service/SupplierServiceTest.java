
package com.agroshop.agroshop.service;

import com.agroshop.agroshop.Entity.Supplier;
import com.agroshop.agroshop.repository.SupplierRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SupplierServiceTest {

    private SupplierRepository repository;
    private SupplierService service;

    // Prepare the test environment
    @BeforeEach
    void setUp() {
        repository = mock(SupplierRepository.class);
        service = new SupplierService(repository);
    }

    // Test 1: Add a new supplier
    @Test
    void testAddSupplier() {

        Supplier supplier = new Supplier(
                "Agro Lanka",
                "0771234567",
                "agro@example.com",
                "Colombo"
        );

        when(repository.save(supplier))
                .thenReturn(supplier);

        Supplier result = service.addSupplier(supplier);

        assertEquals("Agro Lanka", result.getName());
        verify(repository).save(supplier);
    }

    // Test 2: Retrieve all suppliers
    @Test
    void testGetAllSuppliers() {

        Supplier supplier = new Supplier(
                "Agro Lanka",
                "0771234567",
                "agro@example.com",
                "Colombo"
        );

        when(repository.findAll())
                .thenReturn(List.of(supplier));

        List<Supplier> result =
                service.getAllSuppliers();

        assertEquals(1, result.size());
        assertEquals("Agro Lanka", result.get(0).getName());

        verify(repository).findAll();
    }

    // Test 3: Find a supplier by ID
    @Test
    void testGetSupplierById() {

        Supplier supplier = new Supplier(
                "Agro Lanka",
                "0771234567",
                "agro@example.com",
                "Colombo"
        );

        supplier.setSupplierId(1L);

        when(repository.findById(1L))
                .thenReturn(Optional.of(supplier));

        Optional<Supplier> result =
                service.getSupplierById(1L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getSupplierId());
        assertEquals("Agro Lanka", result.get().getName());

        verify(repository).findById(1L);
    }

    // Test 4: Update supplier details
    @Test
    void testUpdateSupplier() {

        Supplier supplier = new Supplier(
                "Updated Agro Lanka",
                "0771234567",
                "agro@example.com",
                "Kandy"
        );

        supplier.setSupplierId(1L);

        when(repository.save(supplier))
                .thenReturn(supplier);

        Supplier result =
                service.updateSupplier(supplier);

        assertEquals("Updated Agro Lanka",
                result.getName());

        assertEquals("Kandy", result.getAddress());

        verify(repository).save(supplier);
    }

    // Test 5: Delete a supplier
    @Test
    void testDeleteSupplier() {

        service.deleteSupplier(1L);

        verify(repository).deleteById(1L);
    }
}
