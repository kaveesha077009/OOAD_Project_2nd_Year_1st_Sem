package com.agroshop.demo.service;

import com.agroshop.demo.dto.CategoryRequest;
import com.agroshop.demo.entity.Category;
import com.agroshop.demo.exception.BadRequestException;
import com.agroshop.demo.exception.ResourceNotFoundException;
import com.agroshop.demo.repository.CategoryRepository;
import com.agroshop.demo.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public CategoryService(CategoryRepository categoryRepository, ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public List<Category> getAll() {
        return categoryRepository.findByStatusTrueOrderByNameAsc();
    }

    @Transactional
    public Category create(CategoryRequest req) {
        String name = req.name().trim();
        if (categoryRepository.existsByNameIgnoreCase(name)) {
            throw new BadRequestException("A category named '" + name + "' already exists");
        }
        Category c = new Category();
        c.setName(name);
        c.setDescription(req.description());
        return categoryRepository.save(c);
    }

    @Transactional
    public Category update(Long id, CategoryRequest req) {
        Category c = find(id);
        String name = req.name().trim();
        if (categoryRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new BadRequestException("A category named '" + name + "' already exists");
        }
        c.setName(name);
        c.setDescription(req.description());
        return categoryRepository.save(c);
    }

    /** Soft delete: the category is hidden, old orders still keep their data. */
    @Transactional
    public void delete(Long id) {
        Category c = find(id);
        if (productRepository.existsByCategoryIdAndStatusTrue(id)) {
            throw new BadRequestException("Move or remove the products in this category first");
        }
        c.setStatus(false);
        categoryRepository.save(c);
    }

    Category find(Long id) {
        return categoryRepository.findById(id)
                .filter(Category::getStatus)
                .orElseThrow(() -> new ResourceNotFoundException("Category " + id + " not found"));
    }
}
