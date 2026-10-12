package com.Agroshop.demo.Services;

import com.Agroshop.demo.Entities.Product;
import com.Agroshop.demo.Repositories.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    public Product addProduct(Product product) {
        return productRepository.save(product);
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public List<Product> getLowStockProducts() {
        return productRepository.findLowStockProducts();
    }

    public Product updateStock(Long id, Integer quantityChange) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        int currentStock = product.getStockQuantity();

        int newStock = currentStock + quantityChange;

        if (newStock < 0) {
            newStock = 0;
        }

        product.setStockQuantity(newStock);

        return productRepository.save(product);
    }
}