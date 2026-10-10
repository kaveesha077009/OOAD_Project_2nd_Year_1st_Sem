package com.agroshop.demo.service;

import com.agroshop.demo.entity.Category;
import com.agroshop.demo.entity.Inventory;
import com.agroshop.demo.entity.Product;
import com.agroshop.demo.exception.BadRequestException;
import com.agroshop.demo.repository.InventoryRepository;
import com.agroshop.demo.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProductServiceTest {

    private InventoryRepository inventoryRepository;
    private ProductService service;
    private Inventory inventory;

    @BeforeEach
    void setUp() {
        ProductRepository productRepository = mock(ProductRepository.class);
        inventoryRepository = mock(InventoryRepository.class);
        CategoryService categoryService = mock(CategoryService.class);
        service = new ProductService(productRepository, inventoryRepository, categoryService);

        Category category = new Category();
        category.setId(1L);
        category.setName("Fertilizer");
        Product product = new Product();
        product.setId(1L);
        product.setName("Urea");
        product.setUnit("kg");
        product.setCategory(category);
        inventory = new Inventory();
        inventory.setProduct(product);
        inventory.setQuantity(new BigDecimal("10.00"));

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(inventoryRepository.findForUpdate(1L)).thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void addingStockIncreasesQuantity() {
        var result = service.updateStock(1L, new BigDecimal("5"));
        assertEquals(0, new BigDecimal("15.00").compareTo(result.quantity()));
    }

    @Test
    void removingMoreThanAvailableIsRejected() {
        assertThrows(BadRequestException.class, () -> service.updateStock(1L, new BigDecimal("-11")));
    }
}
