package com.Agroshop.agroshopmanagementsystem.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id", nullable = true)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "products"})
    private com.Agroshop.agroshopmanagementsystem.entity.Category categoryObj;

    private String category;

    @Column(nullable = false)
    private Double price;

    @Column(nullable = false, length = 20)
    private String unit;

    @Column(name = "image_url", columnDefinition = "LONGTEXT")
    private String imageUrl;

    @Column(name = "in_stock")
    private Boolean inStock = true;

    private Boolean status = true;

    public Product() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public com.Agroshop.agroshopmanagementsystem.entity.Category getCategoryObj() { return categoryObj; }
    public void setCategoryObj(com.Agroshop.agroshopmanagementsystem.entity.Category categoryObj) { this.categoryObj = categoryObj; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public Boolean getInStock() { return inStock; }
    public void setInStock(Boolean inStock) { this.inStock = inStock; }

    public Boolean getStatus() { return status; }
    public void setStatus(Boolean status) { this.status = status; }
}