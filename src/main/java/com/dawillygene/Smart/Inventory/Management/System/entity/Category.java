package com.dawillygene.Smart.Inventory.Management.System.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Category entity for organizing products
 */
@Entity
@Table(name = "categories")
@EntityListeners(AuditingEntityListener.class)
public class Category {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Category name is required")
    @Size(max = 100, message = "Category name must not exceed 100 characters")
    @Column(unique = true, nullable = false)
    private String name;
    
    @Size(max = 20, message = "Category code must not exceed 20 characters")
    @Column(unique = true)
    private String code;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Product> products = new ArrayList<>();
    
    // Transient field for product count (not persisted to database)
    @Transient
    private Long productCount;
    
    // Default constructor
    public Category() {}
    
    // All args constructor
    public Category(Long id, String name, String code, String description, Boolean isActive, LocalDateTime createdAt, LocalDateTime updatedAt, List<Product> products) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.description = description;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.products = products != null ? products : new ArrayList<>();
    }

    // Constructor for creating category with name
    public Category(String name) {
        this.name = name;
        this.isActive = true;
        this.products = new ArrayList<>();
    }

    public Category(String name, String description) {
        this.name = name;
        this.description = description;
        this.isActive = true;
        this.products = new ArrayList<>();
    }
    
    public Category(String name, String code, String description) {
        this.name = name;
        this.code = code;
        this.description = description;
        this.isActive = true;
        this.products = new ArrayList<>();
    }
    
    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public List<Product> getProducts() { return products; }
    public void setProducts(List<Product> products) { this.products = products != null ? products : new ArrayList<>(); }

    public Long getProductCount() { return productCount; }
    public void setProductCount(Long productCount) { this.productCount = productCount; }
    
    // Helper method to get actual product count from the collection
    public int getActualProductCount() {
        return products != null ? products.size() : 0;
    }

    // Helper method for active status (convenience method)
    public boolean isActive() {
        return isActive != null && isActive;
    }
}
