package com.agroshop.agroshopmangementsystem.repository;

import com.agroshop.agroshopmangementsystem.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    // Find a supplier using their phone number
    Optional<Supplier> findByPhone(String phone);
}
