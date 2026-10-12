package com.agroshop.demo.controller;

import com.agroshop.demo.dto.CategoryRequest;
import com.agroshop.demo.entity.Category;
import com.agroshop.demo.service.CategoryService;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** REST endpoints for managing product categories. */
@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private static final Logger log = LoggerFactory.getLogger(CategoryController.class);

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    /** List all categories. */
    @GetMapping
    public List<Category> getAll() {
        return categoryService.getAll();
    }

    /** Create a new category. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Category create(@Valid @RequestBody CategoryRequest request) {
        log.info("Creating category");
        return categoryService.create(request);
    }

    /** Update an existing category. */
    @PutMapping("/{id}")
    public Category update(@PathVariable("id") Long id,
                           @Valid @RequestBody CategoryRequest request) {
        log.info("Updating category {}", id);
        return categoryService.update(id, request);
    }

    /** Delete a category. */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") Long id) {
        log.info("Deleting category {}", id);
        categoryService.delete(id);
    }
}
