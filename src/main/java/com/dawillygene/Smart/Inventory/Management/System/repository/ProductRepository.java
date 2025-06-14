package com.dawillygene.Smart.Inventory.Management.System.repository;

import com.dawillygene.Smart.Inventory.Management.System.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Product entity
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    /**
     * Find product by SKU
     */
    Optional<Product> findBySku(String sku);
    
    /**
     * Find product by barcode
     */
    Optional<Product> findByBarcode(String barcode);
    
    /**
     * Find all active products
     */
    @Query("SELECT p FROM Product p WHERE p.isActive = true")
    List<Product> findAllActiveProducts();
    
    /**
     * Find products by category
     */
    @Query("SELECT p FROM Product p WHERE p.category.id = :categoryId AND p.isActive = true")
    List<Product> findByCategoryId(@Param("categoryId") Long categoryId);
    
    /**
     * Find products by supplier
     */
    @Query("SELECT p FROM Product p WHERE p.supplier.id = :supplierId AND p.isActive = true")
    List<Product> findBySupplierId(@Param("supplierId") Long supplierId);
    
    /**
     * Search products by name or description
     */
    @Query("SELECT p FROM Product p WHERE (LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND p.isActive = true")
    List<Product> searchByKeyword(@Param("keyword") String keyword);
    
    /**
     * Find products with low stock
     */
    @Query("SELECT p FROM Product p JOIN p.inventory i WHERE i.quantity <= p.minimumStockLevel AND p.isActive = true")
    List<Product> findProductsWithLowStock();
    
    /**
     * Find products with overstock
     */
    @Query("SELECT p FROM Product p JOIN p.inventory i WHERE i.quantity >= p.maximumStockLevel AND p.isActive = true")
    List<Product> findProductsWithOverstock();
    
    /**
     * Find products by price range
     */
    @Query("SELECT p FROM Product p WHERE p.price BETWEEN :minPrice AND :maxPrice AND p.isActive = true")
    List<Product> findByPriceRange(@Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice);
    
    /**
     * Check if SKU exists
     */
    boolean existsBySku(String sku);
    
    /**
     * Check if barcode exists
     */
    boolean existsByBarcode(String barcode);
    
    /**
     * Count products by category
     */
    @Query("SELECT COUNT(p) FROM Product p WHERE p.category.id = :categoryId AND p.isActive = true")
    Long countByCategoryId(@Param("categoryId") Long categoryId);
    
    /**
     * Count products by supplier
     */
    @Query("SELECT COUNT(p) FROM Product p WHERE p.supplier.id = :supplierId AND p.isActive = true")
    Long countBySupplierId(@Param("supplierId") Long supplierId);
}
