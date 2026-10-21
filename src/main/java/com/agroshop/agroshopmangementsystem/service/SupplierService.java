package com.agroshop.agroshopmangementsystem.service;

import com.agroshop.agroshopmangementsystem.entity.Supplier;
import com.agroshop.agroshopmangementsystem.repository.SupplierRepository;
<<<<<<< HEAD
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
=======
import com.agroshop.agroshopmangementsystem.repository.PurchaseOrderRepository;

import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
>>>>>>> origin/feature/supplier-purchasing-integration

@Service
public class SupplierService {

<<<<<<< HEAD
    @Autowired
    private SupplierRepository supplierRepository;

=======
    private final SupplierRepository supplierRepository;
    private final PurchaseOrderRepository orderRepository;

    public SupplierService(
            SupplierRepository supplierRepository,
            PurchaseOrderRepository orderRepository) {

        this.supplierRepository = supplierRepository;
        this.orderRepository = orderRepository;
    }

    // Validate supplier details
    private void validateSupplier(Supplier supplier) {

        if (supplier == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Supplier details are required");
        }

        if (supplier.getName() == null ||
                supplier.getName().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Supplier name cannot be empty");
        }

        String phone = supplier.getPhone();

        if (phone == null ||
                !phone.matches("(?:0|\\+94)7\\d{8}")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid mobile number");
        }

        String email = supplier.getEmail();

        if (email != null && !email.isBlank() &&
                !email.matches(
                        "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid email address");
        }
    }

    // Prevent duplicate supplier phone numbers
    private void checkDuplicatePhone(Supplier supplier) {

        Optional<Supplier> existing =
                supplierRepository.findByPhone(supplier.getPhone());

        if (existing.isPresent() &&
                !existing.get().getId().equals(supplier.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Phone number already registered");
        }
    }

    // Add supplier
    @Transactional
    public Supplier addSupplier(Supplier supplier) {

        validateSupplier(supplier);
        supplier.setId(null);
        checkDuplicatePhone(supplier);

        return supplierRepository.save(supplier);
    }

    // Get all suppliers
>>>>>>> origin/feature/supplier-purchasing-integration
    public List<Supplier> getAllSuppliers() {
        return supplierRepository.findAll();
    }

<<<<<<< HEAD
    public Supplier addSupplier(Supplier supplier) {
        if (supplier.getStatus() == null) {
            supplier.setStatus(true);
        }
        return supplierRepository.save(supplier);
    }

    public void deleteSupplier(Long id) {
        supplierRepository.deleteById(id);
    }
}
=======
    // Find supplier by ID
    public Optional<Supplier> getSupplierById(Long id) {
        return supplierRepository.findById(id);
    }

    // Update supplier
    @Transactional
    public Supplier updateSupplier(Supplier supplier) {

        validateSupplier(supplier);

        Long id = supplier.getId();

        if (id == null || !supplierRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Supplier not found");
        }

        checkDuplicatePhone(supplier);
        return supplierRepository.save(supplier);
    }

    // Delete supplier safely
    @Transactional
    public void deleteSupplier(Long id) {

        if (!supplierRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Supplier not found");
        }

        if (orderRepository.existsBySupplierId(id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Cannot delete supplier with purchase orders");
        }

        supplierRepository.deleteById(id);
    }
}
>>>>>>> origin/feature/supplier-purchasing-integration
