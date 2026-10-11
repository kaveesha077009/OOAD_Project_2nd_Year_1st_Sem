
package com.agroshop.agroshop.repository;

import com.agroshop.agroshop.Entity.PurchaseOrder;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;
import java.util.Optional;

public interface PurchaseOrderRepository
        extends JpaRepository<PurchaseOrder, Long> {

    // Used to prevent deletion of suppliers with orders
    boolean existsBySupplier_SupplierId(Long supplierId);

    // Load purchase orders together with their items
    @Override
    @EntityGraph(attributePaths = {"items"})
    List<PurchaseOrder> findAll();

    // Load a single purchase order with its items
    @Override
    @EntityGraph(attributePaths = {"items"})
    Optional<PurchaseOrder> findById(Long id);
}
