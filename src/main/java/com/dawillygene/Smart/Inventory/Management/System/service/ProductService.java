package com.dawillygene.Smart.Inventory.Management.System.service;

import com.dawillygene.Smart.Inventory.Management.System.entity.Product;
import com.dawillygene.Smart.Inventory.Management.System.entity.Inventory;
import com.dawillygene.Smart.Inventory.Management.System.repository.ProductRepository;
import com.dawillygene.Smart.Inventory.Management.System.repository.InventoryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Service class for Product management operations
 */
@Service
@Transactional
public class ProductService {
    
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    
    public ProductService(ProductRepository productRepository, InventoryRepository inventoryRepository) {
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
    }
    
    /**
     * Create a new product
     */
    public Product createProduct(Product product) {
        // Save the product first
        Product savedProduct = productRepository.save(product);
        
        // Create corresponding inventory record
        Inventory inventory = new Inventory(savedProduct, 0);
        inventoryRepository.save(inventory);
        
        return savedProduct;
    }
    
    /**
     * Update an existing product
     */
    public Product updateProduct(Long productId, Product productDetails) {
        Product existingProduct = getProductById(productId);
        
        existingProduct.setName(productDetails.getName());
        existingProduct.setDescription(productDetails.getDescription());
        existingProduct.setSku(productDetails.getSku());
        existingProduct.setBarcode(productDetails.getBarcode());
        existingProduct.setPrice(productDetails.getPrice());
        existingProduct.setCost(productDetails.getCost());
        existingProduct.setUnitOfMeasure(productDetails.getUnitOfMeasure());
        existingProduct.setMinimumStockLevel(productDetails.getMinimumStockLevel());
        existingProduct.setMaximumStockLevel(productDetails.getMaximumStockLevel());
        existingProduct.setReorderPoint(productDetails.getReorderPoint());
        existingProduct.setWeight(productDetails.getWeight());
        existingProduct.setDimensions(productDetails.getDimensions());
        existingProduct.setImageUrl(productDetails.getImageUrl());
        existingProduct.setIsActive(productDetails.getIsActive());
        existingProduct.setIsPerishable(productDetails.getIsPerishable());
        existingProduct.setShelfLifeDays(productDetails.getShelfLifeDays());
        existingProduct.setCategory(productDetails.getCategory());
        existingProduct.setSupplier(productDetails.getSupplier());
        
        return productRepository.save(existingProduct);
    }
    
    /**
     * Save product (create or update)
     */
    public Product saveProduct(Product product) {
        if (product.getId() == null) {
            return createProduct(product);
        } else {
            return updateProduct(product.getId(), product);
        }
    }
    
    /**
     * Get product by ID
     */
    @Transactional(readOnly = true)
    public Product getProductById(Long productId) {
        return productRepository.findById(productId)
            .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));
    }
    
    /**
     * Find product by ID
     */
    @Transactional(readOnly = true)
    public Optional<Product> findProductById(Long id) {
        return productRepository.findById(id);
    }
    
    /**
     * Get product by SKU
     */
    @Transactional(readOnly = true)
    public Optional<Product> getProductBySku(String sku) {
        return productRepository.findBySku(sku);
    }
    
    /**
     * Get product by barcode
     */
    @Transactional(readOnly = true)
    public Optional<Product> getProductByBarcode(String barcode) {
        return productRepository.findByBarcode(barcode);
    }
    
    /**
     * Get all products
     */
    @Transactional(readOnly = true)
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }
    
    /**
     * Get all active products
     */
    @Transactional(readOnly = true)
    public List<Product> getAllActiveProducts() {
        return productRepository.findAllActiveProducts();
    }
    
    /**
     * Get products by category
     */
    @Transactional(readOnly = true)
    public List<Product> getProductsByCategory(Long categoryId) {
        return productRepository.findByCategoryId(categoryId);
    }
    
    /**
     * Get products by supplier
     */
    @Transactional(readOnly = true)
    public List<Product> getProductsBySupplier(Long supplierId) {
        return productRepository.findBySupplierId(supplierId);
    }
    
    /**
     * Search products by keyword
     */
    @Transactional(readOnly = true)
    public List<Product> searchProducts(String keyword) {
        return productRepository.searchByKeyword(keyword);
    }
    
    /**
     * Get products with low stock
     */
    @Transactional(readOnly = true)
    public List<Product> getProductsWithLowStock() {
        return productRepository.findProductsWithLowStock();
    }
    
    /**
     * Get products with overstock
     */
    @Transactional(readOnly = true)
    public List<Product> getProductsWithOverstock() {
        return productRepository.findProductsWithOverstock();
    }
    
    /**
     * Get products by price range
     */
    @Transactional(readOnly = true)
    public List<Product> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        return productRepository.findByPriceRange(minPrice, maxPrice);
    }
    
    /**
     * Find all products with pagination
     */
    @Transactional(readOnly = true)
    public Page<Product> findAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable);
    }
    
    /**
     * Search products with pagination
     */
    @Transactional(readOnly = true)
    public Page<Product> searchProducts(String keyword, Pageable pageable) {
        // For now, return all products as Page - we can implement proper search later
        return productRepository.findAll(pageable);
    }
    
    /**
     * Find products by category with pagination
     */
    @Transactional(readOnly = true)
    public Page<Product> findProductsByCategory(Long categoryId, Pageable pageable) {
        // Simplified for now - return all products with pagination
        return productRepository.findAll(pageable);
    }
    
    /**
     * Find products by supplier with pagination
     */
    @Transactional(readOnly = true)
    public Page<Product> findProductsBySupplier(Long supplierId, Pageable pageable) {
        // Simplified for now - return all products with pagination
        return productRepository.findAll(pageable);
    }
    
    /**
     * Find active products by category
     */
    @Transactional(readOnly = true)
    public List<Product> findActiveProductsByCategory(Long categoryId) {
        return productRepository.findByCategoryId(categoryId);
    }
    
    /**
     * Find active products by supplier
     */
    @Transactional(readOnly = true)
    public List<Product> findActiveProductsBySupplier(Long supplierId) {
        return productRepository.findBySupplierId(supplierId);
    }
    
    /**
     * Delete product by ID
     */
    public void deleteProduct(Long id) {
        Product product = getProductById(id);
        productRepository.delete(product);
    }
    
    /**
     * Deactivate product (soft delete)
     */
    public void deactivateProduct(Long productId) {
        Product product = getProductById(productId);
        product.setIsActive(false);
        productRepository.save(product);
    }
    
    /**
     * Activate product
     */
    public void activateProduct(Long productId) {
        Product product = getProductById(productId);
        product.setIsActive(true);
        productRepository.save(product);
    }
    
    /**
     * Check if SKU exists
     */
    @Transactional(readOnly = true)
    public boolean existsBySku(String sku) {
        return productRepository.existsBySku(sku);
    }
    
    /**
     * Check if barcode exists
     */
    @Transactional(readOnly = true)
    public boolean existsByBarcode(String barcode) {
        return productRepository.existsByBarcode(barcode);
    }
    
    /**
     * Count products by category
     */
    @Transactional(readOnly = true)
    public Long countProductsByCategory(Long categoryId) {
        return productRepository.countByCategoryId(categoryId);
    }
    
    /**
     * Count products by supplier
     */
    @Transactional(readOnly = true)
    public Long countProductsBySupplier(Long supplierId) {
        return productRepository.countBySupplierId(supplierId);
    }
    
    /**
     * Generate unique SKU
     */
    public String generateUniqueSku(String prefix) {
        String baseSku = prefix.toUpperCase();
        int counter = 1;
        String sku = baseSku + String.format("%04d", counter);
        
        while (existsBySku(sku)) {
            counter++;
            sku = baseSku + String.format("%04d", counter);
        }
        
        return sku;
    }
}
