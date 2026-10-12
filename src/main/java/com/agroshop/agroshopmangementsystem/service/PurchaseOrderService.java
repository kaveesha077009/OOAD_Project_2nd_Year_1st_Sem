package com.agroshop.agroshopmangementsystem.service;

import com.agroshop.agroshopmangementsystem.dto.PurchaseOrderRequest;
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
import java.util.List;

@Service
public class PurchaseOrderService {

    private final PurchaseOrderRepository orderRepository;
    private final PurchaseItemRepository itemRepository;
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;

    public PurchaseOrderService(
            PurchaseOrderRepository orderRepository,
            PurchaseItemRepository itemRepository,
            SupplierRepository supplierRepository,
            ProductRepository productRepository) {

        this.orderRepository = orderRepository;
        this.itemRepository = itemRepository;
        this.supplierRepository = supplierRepository;
        this.productRepository = productRepository;
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

    // Mark an order as received
    @Transactional
    public PurchaseOrder receivePurchaseOrder(Long id) {

        PurchaseOrder order = getPurchaseOrderById(id);

        if (!"PENDING".equals(order.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Order is not pending");
        }

        order.setStatus("RECEIVED");

        return orderRepository.save(order);
    }
}
