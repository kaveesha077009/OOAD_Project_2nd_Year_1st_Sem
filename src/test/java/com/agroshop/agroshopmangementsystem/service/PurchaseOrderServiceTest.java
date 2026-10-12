package com.agroshop.agroshopmangementsystem.service;

import com.agroshop.agroshopmangementsystem.dto.PurchaseOrderRequest;
import com.agroshop.agroshopmangementsystem.entity.*;
import com.agroshop.agroshopmangementsystem.repository.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PurchaseOrderServiceTest {

    private PurchaseOrderRepository orderRepository;
    private PurchaseItemRepository itemRepository;
    private SupplierRepository supplierRepository;
    private ProductRepository productRepository;
    private InventoryRepository inventoryRepository;
    private PurchaseOrderService service;

    @BeforeEach
    void setUp() {
        orderRepository = mock(PurchaseOrderRepository.class);
        itemRepository = mock(PurchaseItemRepository.class);
        supplierRepository = mock(SupplierRepository.class);
        productRepository = mock(ProductRepository.class);
        inventoryRepository = mock(InventoryRepository.class);

        service = new PurchaseOrderService(
                orderRepository,
                itemRepository,
                supplierRepository,
                productRepository,
                inventoryRepository
        );
    }

    // Helper to create an item request
    private PurchaseOrderRequest.Item item(
            Long productId, int quantity, String price) {

        PurchaseOrderRequest.Item item = new PurchaseOrderRequest.Item();
        item.setProductId(productId);
        item.setQuantity(quantity);
        item.setUnitPrice(new BigDecimal(price));
        return item;
    }

    // Helper to create an order request
    private PurchaseOrderRequest request(
            PurchaseOrderRequest.Item... items) {

        PurchaseOrderRequest request = new PurchaseOrderRequest();
        request.setSupplierId(2L);
        request.setItems(List.of(items));
        return request;
    }

    @Test
    void shouldCreateOrderWithCorrectTotal() {
        PurchaseOrderRequest request = request(
                item(1L, 10, "1500"),
                item(2L, 5, "750")
        );

        when(supplierRepository.existsById(2L)).thenReturn(true);
        when(productRepository.existsById(anyLong())).thenReturn(true);

        when(orderRepository.save(any(PurchaseOrder.class)))
                .thenAnswer(invocation -> {
                    PurchaseOrder order = invocation.getArgument(0);
                    order.setId(3L);
                    return order;
                });

        PurchaseOrder result = service.createPurchaseOrder(request);

        assertEquals(18750.0, result.getTotalAmount());
        assertEquals("PENDING", result.getStatus());
        assertEquals(2L, result.getSupplierId());

        verify(itemRepository).saveAll(argThat(items -> {
            int count = 0;
            for (PurchaseItem savedItem : items) {
                if (!Long.valueOf(3L).equals(
                        savedItem.getPurchaseOrderId())) {
                    return false;
                }
                count++;
            }
            return count == 2;
        }));
    }

    @Test
    void shouldRejectInvalidQuantity() {
        PurchaseOrderRequest request =
                request(item(1L, 0, "1500"));

        when(supplierRepository.existsById(2L)).thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.createPurchaseOrder(request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void shouldRejectNonexistentProduct() {
        PurchaseOrderRequest request =
                request(item(99L, 2, "500"));

        when(supplierRepository.existsById(2L)).thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.createPurchaseOrder(request)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void shouldReplenishInventoryWhenOrderReceived() {
        PurchaseOrder order = new PurchaseOrder();
        order.setId(3L);
        order.setStatus("PENDING");

        PurchaseItem purchaseItem = new PurchaseItem();
        purchaseItem.setProductId(1L);
        purchaseItem.setQuantity(10);

        Inventory inventory = new Inventory();
        inventory.setProductId(1L);
        inventory.setQuantity(5);

        Product product = new Product();
        product.setId(1L);

        when(orderRepository.findByIdForUpdate(3L))
                .thenReturn(Optional.of(order));
        when(itemRepository.findByPurchaseOrderId(3L))
                .thenReturn(List.of(purchaseItem));
        when(productRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(product));
        when(inventoryRepository.findByProductIdForUpdate(1L))
                .thenReturn(Optional.of(inventory));
        when(orderRepository.save(order)).thenReturn(order);

        PurchaseOrder result = service.receivePurchaseOrder(3L);

        assertEquals("RECEIVED", result.getStatus());
        assertEquals(15, inventory.getQuantity());
        verify(inventoryRepository).save(inventory);
    }

    @Test
    void shouldPreventReceivingOrderTwice() {
        PurchaseOrder order = new PurchaseOrder();
        order.setId(3L);
        order.setStatus("RECEIVED");

        when(orderRepository.findByIdForUpdate(3L))
                .thenReturn(Optional.of(order));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.receivePurchaseOrder(3L)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(inventoryRepository, never()).save(any());
    }

    @Test
    void shouldCreateInventoryIfMissing() {
        PurchaseOrder order = new PurchaseOrder();
        order.setId(4L);
        order.setStatus("PENDING");

        PurchaseItem purchaseItem = new PurchaseItem();
        purchaseItem.setProductId(2L);
        purchaseItem.setQuantity(7);

        Product product = new Product();
        product.setId(2L);

        when(orderRepository.findByIdForUpdate(4L))
                .thenReturn(Optional.of(order));
        when(itemRepository.findByPurchaseOrderId(4L))
                .thenReturn(List.of(purchaseItem));
        when(productRepository.findByIdForUpdate(2L))
                .thenReturn(Optional.of(product));
        when(inventoryRepository.findByProductIdForUpdate(2L))
                .thenReturn(Optional.empty());
        when(orderRepository.save(order)).thenReturn(order);

        service.receivePurchaseOrder(4L);

        verify(inventoryRepository).save(argThat(inventory ->
                inventory.getProductId().equals(2L)
                && inventory.getQuantity() == 7
                && inventory.getMinimumStock() == 5
        ));

        assertEquals("RECEIVED", order.getStatus());
    }
}
