
package com.agroshop.agroshop.controller;

import com.agroshop.agroshop.Entity.Supplier;
import com.agroshop.agroshop.service.SupplierService;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/suppliers")
public class SupplierController {

    private final SupplierService supplierService;

    public SupplierController(
            SupplierService supplierService) {

        this.supplierService = supplierService;
    }

    // Add a supplier
    @PostMapping
    public Supplier addSupplier(
            @RequestBody Supplier supplier) {

        return supplierService.addSupplier(supplier);
    }

    // Get all suppliers
    @GetMapping
    public List<Supplier> getAllSuppliers() {

        return supplierService.getAllSuppliers();
    }

    // Get supplier by ID
    @GetMapping("/{id}")
    public Supplier getSupplierById(
            @PathVariable Long id) {

        return supplierService.getSupplierById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Supplier not found"
                        )
                );
    }

    // Update supplier
    @PutMapping("/{id}")
    public Supplier updateSupplier(
            @PathVariable Long id,
            @RequestBody Supplier supplier) {

        supplier.setSupplierId(id);

        return supplierService.updateSupplier(supplier);
    }

    // Delete supplier
    @DeleteMapping("/{id}")
    public void deleteSupplier(
            @PathVariable Long id) {

        supplierService.deleteSupplier(id);
    }
}
