package com.agroshop.demo.repository;

import com.agroshop.demo.entity.Inventory;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    Optional<Inventory> findByProductId(Long productId);

    /** Row lock, so two stock updates at the same time cannot overwrite each other. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Inventory i WHERE i.product.id = :productId")
    Optional<Inventory> findForUpdate(@Param("productId") Long productId);

    @Query("SELECT i FROM Inventory i JOIN FETCH i.product p JOIN FETCH p.category "
            + "WHERE p.status = true AND i.quantity <= i.minimumStock ORDER BY i.quantity")
    List<Inventory> findLowStock();
}
