package com.agroshop.agroshopmangementsystem.repository;

import com.agroshop.agroshopmangementsystem.entity.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {
    List<PurchaseOrder> findBySupplierId(Long supplierId);
}