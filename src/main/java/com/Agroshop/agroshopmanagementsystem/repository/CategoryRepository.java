package com.Agroshop.agroshopmanagementsystem.repository;

import com.Agroshop.agroshopmanagementsystem.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {}