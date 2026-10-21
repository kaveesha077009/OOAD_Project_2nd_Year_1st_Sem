package com.agroshop.agroshopmangementsystem.service;

<<<<<<< HEAD
import com.agroshop.agroshopmangementsystem.entity.PurchaseOrder;
import com.agroshop.agroshopmangementsystem.repository.PurchaseOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
=======
import com.agroshop.agroshopmangementsystem.dto.PurchaseOrderRequest;
import com.agroshop.agroshopmangementsystem.entity.Inventory;
import com.agroshop.agroshopmangementsystem.repository.InventoryRepository;

import com.agroshop.agroshopmangementsystem.entity.PurchaseItem;
import com.agroshop.agroshopmangementsystem.entity.PurchaseOrder;
import com.agroshop.agroshopmangementsystem.repository.ProductRepository;
import com.agroshop.agroshopmangementsystem.repository.PurchaseItemRepository;
import com.agroshop.agroshopmangementsystem.repository.PurchaseOrderRepository;
import com.agroshop.agroshopmangementsystem.repository.SupplierRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
>>>>>>> origin/feature/supplier-purchasing-integration
import java.util.List;

@Service
public class PurchaseOrderService {

<<<<<<< HEAD
    @Autowired
    private PurchaseOrderRepository purchaseOrderRepository;

    public List<PurchaseOrder> getAllPurchaseOrders() {
        return purchaseOrderRepository.findAll();
    }

    public PurchaseOrder createPurchaseOrder(PurchaseOrder order) {
        order.setOrderDate(LocalDateTime.now());
        if (order.getStatus() == null) order.setStatus("PENDING");
        return purchaseOrderRepository.save(order);
    }
}
=======
    private final PurchaseOrderRepository orderRepository;
    private final PurchaseItemRepository itemRepository;
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;

    public PurchaseOrderService(
            PurchaseOrderRepository orderRepository,
            PurchaseItemRepository itemRepository,
            SupplierRepository supplierRepository,
            ProductRepository productRepository,
            InventoryRepository inventoryRepository) {

        this.orderRepository = orderRepository;
        this.itemRepository = itemRepository;
        this.supplierRepository = supplierRepository;
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
    }

    // Get all purchase orders
    public List<PurchaseOrder> getAllPurchaseOrders() {
        return orderRepository.findAll();
    }

    // Get one purchase order
    public PurchaseOrder getPurchaseOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Purchase order not found"));
    }

    // Get products belonging to a purchase order
    public List<PurchaseItem> getItemsByOrderId(Long orderId) {
        getPurchaseOrderById(orderId);
        return itemRepository.findByPurchaseOrderId(orderId);
    }

    // Create an order containing multiple products
    @Transactional
    public PurchaseOrder createPurchaseOrder(PurchaseOrderRequest request) {

        if (request == null || request.getSupplierId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Supplier is required");
        }

        if (!supplierRepository.existsById(request.getSupplierId())) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Supplier not found");
        }

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "At least one product is required");
        }

        BigDecimal total = BigDecimal.ZERO;
        List<PurchaseItem> items = new ArrayList<>();

        for (PurchaseOrderRequest.Item requestedItem : request.getItems()) {

            if (requestedItem == null || requestedItem.getProductId() == null
                    || requestedItem.getQuantity() == null
                    || requestedItem.getQuantity() <= 0
                    || requestedItem.getUnitPrice() == null
                    || requestedItem.getUnitPrice().signum() < 0) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Invalid purchase item");
            }

            if (!productRepository.existsById(requestedItem.getProductId())) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Product not found");
            }

            BigDecimal lineTotal = requestedItem.getUnitPrice()
                    .multiply(BigDecimal.valueOf(requestedItem.getQuantity()));

            total = total.add(lineTotal);

            PurchaseItem item = new PurchaseItem();
            item.setProductId(requestedItem.getProductId());
            item.setQuantity(requestedItem.getQuantity());
            item.setUnitPrice(requestedItem.getUnitPrice().doubleValue());

            items.add(item);
        }

        if (total.doubleValue() == Double.POSITIVE_INFINITY) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Order total is too large");
        }

        PurchaseOrder order = new PurchaseOrder();
        order.setSupplierId(request.getSupplierId());
        order.setOrderDate(LocalDateTime.now());
        order.setStatus("PENDING");
        order.setTotalAmount(total.doubleValue());

        PurchaseOrder savedOrder = orderRepository.save(order);

        for (PurchaseItem item : items) {
            item.setPurchaseOrderId(savedOrder.getId());
        }

        itemRepository.saveAll(items);

        return savedOrder;
    }


    // Receive an order and replenish inventory
    @Transactional
    public PurchaseOrder receivePurchaseOrder(Long id) {

        // Prevent simultaneous receiving of the same order
        PurchaseOrder order = orderRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Purchase order not found"));

        if (!"PENDING".equals(order.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Order has already been received");
        }

        List<PurchaseItem> items =
                itemRepository.findByPurchaseOrderId(id);

        if (items.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Purchase order has no items");
        }

        // Combine repeated products before updating stock
        java.util.Map<Long, Integer> receivedQuantities =
                new java.util.TreeMap<>();

        for (PurchaseItem item : items) {
            if (item.getProductId() == null
                    || item.getQuantity() == null
                    || item.getQuantity() <= 0) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Invalid purchase item");
            }

            try {
                receivedQuantities.merge(
                        item.getProductId(),
                        item.getQuantity(),
                        Math::addExact
                );
            } catch (ArithmeticException e) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Quantity is too large");
            }
        }

        for (var entry : receivedQuantities.entrySet()) {
            Long productId = entry.getKey();
            int receivedQuantity = entry.getValue();

            // Lock the product to coordinate inventory creation
            productRepository.findByIdForUpdate(productId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Product not found"));

            Inventory inventory = inventoryRepository
                    .findByProductIdForUpdate(productId)
                    .orElseGet(() -> {
                        Inventory newInventory = new Inventory();
                        newInventory.setProductId(productId);
                        newInventory.setQuantity(0);
                        newInventory.setMinimumStock(5);
                        return newInventory;
                    });

            int currentQuantity =
                    inventory.getQuantity() == null ? 0 : inventory.getQuantity();

            try {
                inventory.setQuantity(
                        Math.addExact(currentQuantity, receivedQuantity)
                );
            } catch (ArithmeticException e) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Stock quantity is too large");
            }

            inventoryRepository.save(inventory);
        }

        order.setStatus("RECEIVED");
        return orderRepository.save(order);
    }
}
>>>>>>> origin/feature/supplier-purchasing-integration
