package com.agroshop.demo.service;

import com.agroshop.demo.dto.ProductRequest;
import com.agroshop.demo.dto.ProductResponse;
import com.agroshop.demo.entity.Inventory;
import com.agroshop.demo.entity.Product;
import com.agroshop.demo.exception.BadRequestException;
import com.agroshop.demo.exception.ResourceNotFoundException;
import com.agroshop.demo.repository.InventoryRepository;
import com.agroshop.demo.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final CategoryService categoryService;

    public ProductService(ProductRepository productRepository,
                          InventoryRepository inventoryRepository,
                          CategoryService categoryService) {
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
        this.categoryService = categoryService;
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getAll() {
        Map<Long, Inventory> stock = inventoryRepository.findAll().stream()
                .collect(Collectors.toMap(i -> i.getProduct().getId(), Function.identity()));
        return productRepository.findAllActive().stream()
                .map(p -> ProductResponse.of(p, stock.get(p.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getLowStock() {
        return inventoryRepository.findLowStock().stream()
                .map(i -> ProductResponse.of(i.getProduct(), i))
                .toList();
    }

    @Transactional
    public ProductResponse create(ProductRequest req) {
        Product p = new Product();
        apply(p, req);
        p = productRepository.save(p);

        Inventory inv = new Inventory();
        inv.setProduct(p);
        inv.setQuantity(orDefault(req.quantity(), BigDecimal.ZERO));
        inv.setMinimumStock(orDefault(req.minimumStock(), new BigDecimal("5.00")));
        return ProductResponse.of(p, inventoryRepository.save(inv));
    }

    @Transactional
    public ProductResponse update(Long id, ProductRequest req) {
        Product p = findActive(id);
        apply(p, req);
        Inventory inv = inventoryRepository.findByProductId(id).orElseThrow(
                () -> new ResourceNotFoundException("Stock record for product " + id + " not found"));
        if (req.minimumStock() != null) {
            inv.setMinimumStock(req.minimumStock());
        }
        return ProductResponse.of(productRepository.save(p), inv);
    }

    /** Soft delete: the product is hidden but old orders and purchases still point to it. */
    @Transactional
    public void delete(Long id) {
        Product p = findActive(id);
        p.setStatus(false);
        productRepository.save(p);
    }

    /** Adds (or removes, when negative) stock. Refuses to go below zero. */
    @Transactional
    public ProductResponse updateStock(Long id, BigDecimal change) {
        Product p = findActive(id);
        Inventory inv = inventoryRepository.findForUpdate(id).orElseThrow(
                () -> new ResourceNotFoundException("Stock record for product " + id + " not found"));
        BigDecimal newQty = inv.getQuantity().add(change);
        if (newQty.signum() < 0) {
            throw new BadRequestException("Not enough stock. Only " + inv.getQuantity() + " " + p.getUnit() + " left");
        }
        inv.setQuantity(newQty);
        return ProductResponse.of(p, inventoryRepository.save(inv));
    }

    private void apply(Product p, ProductRequest req) {
        p.setCategory(categoryService.find(req.categoryId()));
        p.setName(req.name().trim());
        p.setDescription(req.description());
        p.setPrice(req.price());
        p.setUnit(req.unit().trim());
        p.setImageUrl(req.imageUrl());
    }

    private Product findActive(Long id) {
        return productRepository.findById(id)
                .filter(Product::getStatus)
                .orElseThrow(() -> new ResourceNotFoundException("Product " + id + " not found"));
    }

    private static BigDecimal orDefault(BigDecimal v, BigDecimal d) {
        return v == null ? d : v;
    }
}
