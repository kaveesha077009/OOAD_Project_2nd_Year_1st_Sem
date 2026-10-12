package com.agroshop.demo.controller;

import com.agroshop.demo.dto.ProductRequest;
import com.agroshop.demo.dto.ProductResponse;
import com.agroshop.demo.service.ProductService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

/** REST endpoints for managing shop products and their stock levels. */
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private static final Logger log = LoggerFactory.getLogger(ProductController.class);

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /** List all products. */
    @GetMapping
    public List<ProductResponse> getAll() {
        return productService.getAll();
    }

    /** List products whose stock is below their low-stock threshold. */
    @GetMapping("/low-stock")
    public List<ProductResponse> getLowStock() {
        return productService.getLowStock();
    }

    /** Create a new product. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse create(@Valid @RequestBody ProductRequest request) {
        log.info("Creating product");
        return productService.create(request);
    }

    /** Update an existing product. */
    @PutMapping("/{id}")
    public ProductResponse update(@PathVariable("id") Long id,
                                  @Valid @RequestBody ProductRequest request) {
        log.info("Updating product {}", id);
        return productService.update(id, request);
    }

    /** Delete a product. */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") Long id) {
        log.info("Deleting product {}", id);
        productService.delete(id);
    }

    /**
     * Adjust a product's stock.
     *
     * @param quantityChange positive to add stock, negative to remove stock
     */
    @PutMapping("/{id}/stock")
    public ProductResponse updateStock(@PathVariable("id") Long id,
                                       @RequestParam("quantityChange") BigDecimal quantityChange) {
        log.info("Adjusting stock of product {} by {}", id, quantityChange);
        return productService.updateStock(id, quantityChange);
    }
}
