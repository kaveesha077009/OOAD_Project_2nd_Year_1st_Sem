package com.Agroshop.agroshopmanagementsystem.repository;

import com.Agroshop.agroshopmanagementsystem.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {}