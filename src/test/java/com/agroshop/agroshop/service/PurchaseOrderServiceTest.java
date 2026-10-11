
package com.agroshop.agroshop.service;

import com.agroshop.agroshop.Entity.PurchaseOrderItem;
import com.agroshop.agroshop.Entity.PurchaseOrder;
import com.agroshop.agroshop.Entity.Supplier;
import com.agroshop.agroshop.repository.PurchaseOrderRepository;
import com.agroshop.agroshop.repository.SupplierRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PurchaseOrderServiceTest {

    private PurchaseOrderRepository orderRepository;
    private SupplierRepository supplierRepository;
    private PurchaseOrderService service;

    private Supplier supplier;
    private PurchaseOrder order;

    // Prepare test objects before each test
    @BeforeEach
    void setUp() {

        orderRepository = mock(PurchaseOrderRepository.class);
        supplierRepository = mock(SupplierRepository.class);

        service = new PurchaseOrderService(
                orderRepository,
                supplierRepository
        );

        supplier = new Supplier(
                "Agro Lanka",
                "0771234567",
                "agro@example.com",
                "Colombo"
        );

        supplier.setSupplierId(1L);

        order = new PurchaseOrder();
        order.setId(1L);
        order.setSupplier(supplier);
        order.setOrderDate(LocalDate.of(2026, 10, 11));
        order.setProduct("Fertilizer");
        order.setQuantity(10);
        order.setPrice(new BigDecimal("2500.00"));
        order.setStatus("Pending");
    }

    // Test 1: Create a valid purchase order
    @Test
    void testCreatePurchaseOrder() {

        when(supplierRepository.findById(1L))
                .thenReturn(Optional.of(supplier));

        when(orderRepository.save(any(PurchaseOrder.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PurchaseOrder result = service.create(
                1L,
                LocalDate.of(2026, 10, 11),
                "Fertilizer",
                10,
                new BigDecimal("2500.00")
        );

        assertEquals("Fertilizer", result.getProduct());
        assertEquals(10, result.getQuantity());
        assertEquals("Pending", result.getStatus());
        assertEquals(supplier, result.getSupplier());

        verify(orderRepository).save(any(PurchaseOrder.class));
    }

    // Test 2: Retrieve all purchase orders
    @Test
    void testGetAllPurchaseOrders() {

        when(orderRepository.findAll())
                .thenReturn(List.of(order));

        List<PurchaseOrder> result = service.findAll();

        assertEquals(1, result.size());
        assertEquals("Fertilizer", result.get(0).getProduct());

        verify(orderRepository).findAll();
    }

    // Test 3: Find a purchase order by ID
    @Test
    void testGetPurchaseOrderById() {

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        PurchaseOrder result = service.findById(1L);

        assertEquals(1L, result.getId());
        assertEquals("Fertilizer", result.getProduct());

        verify(orderRepository).findById(1L);
    }

    // Test 4: Reject an invalid quantity
    @Test
    void testInvalidQuantity() {

        assertThrows(
                ResponseStatusException.class,
                () -> service.create(
                        1L,
                        LocalDate.now(),
                        "Fertilizer",
                        -5,
                        new BigDecimal("2500.00")
                )
        );

        verify(orderRepository, never())
                .save(any(PurchaseOrder.class));
    }

    // Test 5: Reject a nonexistent supplier
    @Test
    void testSupplierNotFound() {

        when(supplierRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResponseStatusException.class,
                () -> service.create(
                        99L,
                        LocalDate.now(),
                        "Fertilizer",
                        10,
                        new BigDecimal("2500.00")
                )
        );

        verify(orderRepository, never())
                .save(any(PurchaseOrder.class));
    }

    // Test 6: Receive an order
    @Test
    void testReceivePurchaseOrder() {

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        when(orderRepository.save(any(PurchaseOrder.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PurchaseOrder result = service.receive(1L);

        assertEquals("Received", result.getStatus());

        // Receiving the same order twice must fail
        assertThrows(
                ResponseStatusException.class,
                () -> service.receive(1L)
        );

        verify(orderRepository, times(1))
                .save(any(PurchaseOrder.class));
    }


    // Test 7: Create an order with multiple products
    @Test
    void testCreateMultipleProducts() {

        List<PurchaseOrderItem> items = List.of(
                new PurchaseOrderItem(
                        "Fertilizer",
                        10,
                        new BigDecimal("2500.00")
                ),
                new PurchaseOrderItem(
                        "Pesticide",
                        5,
                        new BigDecimal("1800.00")
                ),
                new PurchaseOrderItem(
                        "Sprinkler",
                        8,
                        new BigDecimal("750.00")
                )
        );

        when(supplierRepository.findById(1L))
                .thenReturn(Optional.of(supplier));

        when(orderRepository.save(any(PurchaseOrder.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PurchaseOrder result = service.createMultiple(
                1L,
                LocalDate.of(2026, 10, 11),
                items
        );

        assertEquals(3, result.getItems().size());
        assertEquals("Pending", result.getStatus());
        assertEquals(supplier, result.getSupplier());

        verify(orderRepository).save(any(PurchaseOrder.class));
    }

    // Test 8: Calculate total amount
    @Test
    void testMultipleProductsTotal() {

        PurchaseOrder purchase = new PurchaseOrder();

        purchase.addItem(new PurchaseOrderItem(
                "Fertilizer",
                10,
                new BigDecimal("2500.00")
        ));

        purchase.addItem(new PurchaseOrderItem(
                "Pesticide",
                5,
                new BigDecimal("1800.00")
        ));

        purchase.addItem(new PurchaseOrderItem(
                "Sprinkler",
                8,
                new BigDecimal("750.00")
        ));

        assertEquals(
                0,
                new BigDecimal("40000.00")
                        .compareTo(purchase.getTotalAmount())
        );
    }

    // Test 9: Reject an empty product list
    @Test
    void testEmptyProductList() {

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.createMultiple(
                        1L,
                        LocalDate.now(),
                        List.of()
                )
        );

        assertEquals(400, exception.getStatusCode().value());

        verify(orderRepository, never())
                .save(any(PurchaseOrder.class));
    }

    // Test 10: Reject invalid item quantity
    @Test
    void testMultipleProductsInvalidQuantity() {

        List<PurchaseOrderItem> items = List.of(
                new PurchaseOrderItem(
                        "Fertilizer",
                        -5,
                        new BigDecimal("2500.00")
                )
        );

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.createMultiple(
                        1L,
                        LocalDate.now(),
                        items
                )
        );

        assertEquals(400, exception.getStatusCode().value());

        verify(orderRepository, never())
                .save(any(PurchaseOrder.class));
    }

    // Test 11: Verify order-item relationships
    @Test
    void testPurchaseItemRelationships() {

        List<PurchaseOrderItem> items = List.of(
                new PurchaseOrderItem(
                        "Fertilizer",
                        10,
                        new BigDecimal("2500.00")
                ),
                new PurchaseOrderItem(
                        "Pesticide",
                        5,
                        new BigDecimal("1800.00")
                )
        );

        when(supplierRepository.findById(1L))
                .thenReturn(Optional.of(supplier));

        when(orderRepository.save(any(PurchaseOrder.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PurchaseOrder result = service.createMultiple(
                1L,
                LocalDate.now(),
                items
        );

        // Both items must belong to the same order
        for (PurchaseOrderItem item : result.getItems()) {
            assertSame(result, item.getPurchaseOrder());
        }

        assertEquals(2, result.getItems().size());
    }

}
