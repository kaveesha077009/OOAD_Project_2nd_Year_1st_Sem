package com.agroshop.agroshopmangementsystem.repository;

import com.agroshop.agroshopmangementsystem.entity.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {

    // Find purchase orders belonging to a supplier
    List<PurchaseOrder> findBySupplierId(Long supplierId);

    // Check whether a supplier has any purchase orders
    boolean existsBySupplierId(Long supplierId);
}
