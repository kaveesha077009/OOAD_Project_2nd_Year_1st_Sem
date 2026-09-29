package com.agroshop.agroshop.service;

import com.agroshop.agroshop.Entity.PurchaseOrder;
import com.agroshop.agroshop.Entity.Supplier;
import com.agroshop.agroshop.repository.PurchaseOrderRepository;
import com.agroshop.agroshop.repository.SupplierRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class PurchaseOrderService {
    private final PurchaseOrderRepository orders;
    private final SupplierRepository suppliers;
    public PurchaseOrderService(PurchaseOrderRepository orders, SupplierRepository suppliers) {
        this.orders = orders; this.suppliers = suppliers;
    }
    public List<PurchaseOrder> findAll() { return orders.findAll(); }
    public PurchaseOrder findById(Long id) {
        return orders.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Purchase order not found"));
    }
    @Transactional
    public PurchaseOrder create(Long supplierId, LocalDate date, String product, Integer quantity, BigDecimal price) {
        if (supplierId == null || date == null || product == null || product.isBlank() || quantity == null || quantity < 1 || price == null || price.signum() < 0)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid purchase order");
        Supplier supplier = suppliers.findById(supplierId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Supplier not found"));
        PurchaseOrder order = new PurchaseOrder();
        order.setSupplier(supplier); order.setOrderDate(date); order.setProduct(product.trim());
        order.setQuantity(quantity); order.setPrice(price); order.setStatus("Pending");
        return orders.save(order);
    }
    @Transactional
    public PurchaseOrder receive(Long id) {
        PurchaseOrder order = findById(id);
        if (!"Pending".equals(order.getStatus())) throw new ResponseStatusException(HttpStatus.CONFLICT, "Order already received");
        order.setStatus("Received");
        // Does NOT change inventory. Integrate with product team's stock service later.
        return orders.save(order);
    }
}
