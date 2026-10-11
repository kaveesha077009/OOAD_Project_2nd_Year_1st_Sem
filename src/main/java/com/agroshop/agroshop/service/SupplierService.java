
package com.agroshop.agroshop.service;

import com.agroshop.agroshop.Entity.Supplier;
import com.agroshop.agroshop.repository.SupplierRepository;
import com.agroshop.agroshop.repository.PurchaseOrderRepository;

import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final PurchaseOrderRepository orderRepository;

    public SupplierService(
            SupplierRepository supplierRepository,
            PurchaseOrderRepository orderRepository) {

        this.supplierRepository = supplierRepository;
        this.orderRepository = orderRepository;
    }

    // Validate supplier information
    private void validateSupplier(Supplier supplier) {

        if (supplier == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Supplier details are required"
            );
        }

        // Validate name
        if (supplier.getName() == null ||
                supplier.getName().isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Supplier name cannot be empty"
            );
        }

        // Validate phone
        String phone = supplier.getPhone();

        if (phone == null ||
                !phone.matches("(?:0|\\+94)7\\d{8}")) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid mobile number"
            );
        }

        // Validate optional email
        String email = supplier.getEmail();

        if (email != null && !email.isBlank() &&
                !email.matches(
                        "^[A-Za-z0-9._%+-]+@" +
                                "[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
                )) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid email address"
            );
        }
    }

    // Check for duplicate phone numbers
    private void checkDuplicatePhone(Supplier supplier) {

        Optional<Supplier> existing =
                supplierRepository.findByPhone(
                        supplier.getPhone()
                );

        if (existing.isPresent() &&
                !existing.get().getSupplierId()
                        .equals(supplier.getSupplierId())) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Phone number already registered"
            );
        }
    }

    // Add supplier
    @Transactional
    public Supplier addSupplier(Supplier supplier) {

        validateSupplier(supplier);

        // New suppliers must not supply their own ID
        supplier.setSupplierId(null);

        checkDuplicatePhone(supplier);

        return supplierRepository.save(supplier);
    }

    // Get all suppliers
    public List<Supplier> getAllSuppliers() {

        return supplierRepository.findAll();
    }

    // Find supplier by ID
    public Optional<Supplier> getSupplierById(Long id) {

        return supplierRepository.findById(id);
    }

    // Update supplier
    @Transactional
    public Supplier updateSupplier(Supplier supplier) {

        validateSupplier(supplier);

        Long id = supplier.getSupplierId();

        if (id == null ||
                !supplierRepository.existsById(id)) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Supplier not found"
            );
        }

        checkDuplicatePhone(supplier);

        return supplierRepository.save(supplier);
    }

    // Delete supplier
    @Transactional
    public void deleteSupplier(Long id) {

        if (!supplierRepository.existsById(id)) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Supplier not found"
            );
        }

        // Prevent deleting suppliers with purchases
        if (orderRepository
                .existsBySupplier_SupplierId(id)) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Cannot delete supplier with purchase orders"
            );
        }

        supplierRepository.deleteById(id);
    }
}
