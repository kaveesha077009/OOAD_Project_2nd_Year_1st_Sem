package com.agroshop.agroshopmangementsystem.repository;

import com.agroshop.agroshopmangementsystem.entity.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {

    // Find purchase orders belonging to a supplier
    List<PurchaseOrder> findBySupplierId(Long supplierId);

    // Check whether a supplier has any purchase orders
    boolean existsBySupplierId(Long supplierId);

    // Lock an order while receiving its purchased products
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM PurchaseOrder p WHERE p.id = :id")
    Optional<PurchaseOrder> findByIdForUpdate(@Param("id") Long id);


}
