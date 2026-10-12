package com.agroshop.demo.repository;

import com.agroshop.demo.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("SELECT p FROM Product p JOIN FETCH p.category WHERE p.status = true ORDER BY p.id")
    List<Product> findAllActive();

    boolean existsByCategoryIdAndStatusTrue(Long categoryId);
}
