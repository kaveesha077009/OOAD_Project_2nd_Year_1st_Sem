package com.agroshop.agroshopmangementsystem.service;

import com.agroshop.agroshopmangementsystem.entity.Supplier;
import com.agroshop.agroshopmangementsystem.repository.SupplierRepository;
import com.agroshop.agroshopmangementsystem.repository.PurchaseOrderRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SupplierServiceTest {

    private SupplierRepository supplierRepository;
    private PurchaseOrderRepository orderRepository;
    private SupplierService supplierService;

    @BeforeEach
    void setUp() {
        supplierRepository = mock(SupplierRepository.class);
        orderRepository = mock(PurchaseOrderRepository.class);

        supplierService = new SupplierService(
                supplierRepository, orderRepository
        );
    }

    // Create a valid test supplier
    private Supplier createSupplier() {
        Supplier supplier = new Supplier();
        supplier.setName("Agro Test Supplier");
        supplier.setPhone("0771234567");
        supplier.setEmail("supplier@example.com");
        supplier.setAddress("Colombo");
        supplier.setStatus(true);
        return supplier;
    }

    @Test
    void shouldAddValidSupplier() {
        Supplier supplier = createSupplier();

        when(supplierRepository.findByPhone("0771234567"))
                .thenReturn(Optional.empty());
        when(supplierRepository.save(supplier))
                .thenReturn(supplier);

        Supplier result = supplierService.addSupplier(supplier);

        assertEquals("Agro Test Supplier", result.getName());
        verify(supplierRepository).save(supplier);
    }

    @Test
    void shouldRejectEmptySupplierName() {
        Supplier supplier = createSupplier();
        supplier.setName("");

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> supplierService.addSupplier(supplier)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(supplierRepository, never()).save(any());
    }

    @Test
    void shouldRejectInvalidPhoneNumber() {
        Supplier supplier = createSupplier();
        supplier.setPhone("123");

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> supplierService.addSupplier(supplier)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void shouldRejectDuplicatePhoneNumber() {
        Supplier supplier = createSupplier();

        Supplier existing = createSupplier();
        existing.setId(1L);

        when(supplierRepository.findByPhone("0771234567"))
                .thenReturn(Optional.of(existing));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> supplierService.addSupplier(supplier)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(supplierRepository, never()).save(any());
    }

    @Test
    void shouldPreventDeletingSupplierWithPurchaseOrders() {
        when(supplierRepository.existsById(1L)).thenReturn(true);
        when(orderRepository.existsBySupplierId(1L)).thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> supplierService.deleteSupplier(1L)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(supplierRepository, never()).deleteById(any());
    }

    @Test
    void shouldDeleteSupplierWithoutPurchaseOrders() {
        when(supplierRepository.existsById(1L)).thenReturn(true);
        when(orderRepository.existsBySupplierId(1L)).thenReturn(false);

        supplierService.deleteSupplier(1L);

        verify(supplierRepository).deleteById(1L);
    }
}
