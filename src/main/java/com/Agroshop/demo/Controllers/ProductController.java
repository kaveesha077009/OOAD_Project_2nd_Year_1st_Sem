package com.Agroshop.demo.Controllers;

import com.Agroshop.demo.Entities.Product;
import com.Agroshop.demo.Services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
public class ProductController {

    @Autowired
    private ProductService productService;

    @PostMapping
    public Product createProduct(@RequestBody Product product) {
        return productService.addProduct(product);
    }

    @GetMapping
    public List<Product> getAllProducts() {
        return productService.getAllProducts();
    }

    @GetMapping("/low-stock")
    public List<Product> getLowStockProducts() {
        return productService.getLowStockProducts();
    }

    @PutMapping("/{id}/stock")
    public Product updateStock(
            @PathVariable Long id,
            @RequestParam Integer quantityChange) {

        return productService.updateStock(id, quantityChange);
    }
}