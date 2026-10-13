package com.Agroshop.agroshopmanagementsystem.service;

import com.Agroshop.agroshopmanagementsystem.entity.Product;
import com.Agroshop.agroshopmanagementsystem.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Product addProduct(Product product) {
        if (product.getInStock() == null) product.setInStock(true);
        return productRepository.save(product);
    }

    public Product updateStockStatus(Long id, Boolean inStock) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        product.setInStock(inStock);
        return productRepository.save(product);
    }
}