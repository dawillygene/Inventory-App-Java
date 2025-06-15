package com.dawillygene.Smart.Inventory.Management.System.service;

import com.dawillygene.Smart.Inventory.Management.System.entity.Category;
import com.dawillygene.Smart.Inventory.Management.System.repository.CategoryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CategoryService {
    
    private final CategoryRepository categoryRepository;
    
    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }
    
    /**
     * Get all categories with pagination
     */
    public Page<Category> findAllCategories(Pageable pageable) {
        return categoryRepository.findAll(pageable);
    }
    
    /**
     * Get all active categories (simplified)
     */
    public List<Category> findAllActiveCategories() {
        return categoryRepository.findAll();
    }
    
    /**
     * Find category by ID
     */
    public Optional<Category> findById(Long id) {
        return categoryRepository.findById(id);
    }
    
    /**
     * Find category by name
     */
    public Optional<Category> findByName(String name) {
        return categoryRepository.findByName(name);
    }
    
    /**
     * Search categories (simplified)
     */
    public Page<Category> searchCategories(String searchTerm, Pageable pageable) {
        return categoryRepository.findAll(pageable);
    }
    
    /**
     * Create new category
     */
    public Category createCategory(Category category) {
        // Check if category with the same name already exists
        Optional<Category> existingCategory = categoryRepository.findByName(category.getName());
        if (existingCategory.isPresent()) {
            throw new RuntimeException("Category with name '" + category.getName() + "' already exists");
        }
        
        // Validate required fields
        validateCategory(category);
        
        // Set default values
        if (category.getIsActive() == null) {
            category.setIsActive(true);
        }
        
        return categoryRepository.save(category);
    }
    
    /**
     * Update existing category
     */
    public Category updateCategory(Long id, Category categoryDetails) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with ID: " + id));
        
        // Check if another category with the same name exists
        Optional<Category> existingCategory = categoryRepository.findByName(categoryDetails.getName());
        if (existingCategory.isPresent() && !existingCategory.get().getId().equals(id)) {
            throw new RuntimeException("Another category with name '" + categoryDetails.getName() + "' already exists");
        }
        
        // Validate required fields
        validateCategory(categoryDetails);
        
        // Update fields
        category.setName(categoryDetails.getName());
        category.setCode(categoryDetails.getCode());
        category.setDescription(categoryDetails.getDescription());
        category.setIsActive(categoryDetails.getIsActive());
        
        return categoryRepository.save(category);
    }
    
    /**
     * Delete category
     */
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with ID: " + id));
        
        // For now, just delete - we'll add product count check later
        categoryRepository.delete(category);
    }
    
    /**
     * Get total number of categories
     */
    public Long getTotalCategoryCount() {
        return categoryRepository.count();
    }
    
    /**
     * Validate category data
     */
    public void validateCategory(Category category) {
        if (category.getName() == null || category.getName().trim().isEmpty()) {
            throw new RuntimeException("Category name is required");
        }
        
        if (category.getName().length() > 100) {
            throw new RuntimeException("Category name cannot exceed 100 characters");
        }
        
        if (category.getCode() != null && category.getCode().length() > 20) {
            throw new RuntimeException("Category code cannot exceed 20 characters");
        }
        
        if (category.getDescription() != null && category.getDescription().length() > 500) {
            throw new RuntimeException("Category description cannot exceed 500 characters");
        }
    }
    
    /**
     * Check if category exists by name
     */
    public boolean existsByName(String name) {
        return categoryRepository.findByName(name).isPresent();
    }

    /**
     * Find category by ID (throwing exception if not found)
     */
    public Category findCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with ID: " + id));
    }

    /**
     * Check if another category with the same name exists (excluding current id)
     */
    public boolean existsByNameAndNotId(String name, Long id) {
        Optional<Category> existingCategory = categoryRepository.findByName(name);
        return existingCategory.isPresent() && !existingCategory.get().getId().equals(id);
    }

    /**
     * Save category (wrapper for create/update)
     */
    public Category saveCategory(Category category) {
        // Validate required fields
        validateCategory(category);
        
        // Set default values if needed
        if (category.getIsActive() == null) {
            category.setIsActive(true);
        }
        
        return categoryRepository.save(category);
    }

    /**
     * Get product count for a category
     */
    public long getProductCountByCategory(Long categoryId) {
        Category category = findCategoryById(categoryId);
        return category.getProducts() != null ? category.getProducts().size() : 0;
    }
}
