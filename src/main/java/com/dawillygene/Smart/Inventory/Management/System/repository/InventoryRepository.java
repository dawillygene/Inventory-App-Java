package com.dawillygene.Smart.Inventory.Management.System.repository;

import com.dawillygene.Smart.Inventory.Management.System.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Inventory entity
 */
@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    
    /**
     * Find inventory by product ID
     */
    @Query("SELECT i FROM Inventory i WHERE i.product.id = :productId")
    Optional<Inventory> findByProductId(@Param("productId") Long productId);
    
    /**
     * Find all low stock items
     */
    @Query("SELECT i FROM Inventory i JOIN i.product p WHERE i.quantity <= p.minimumStockLevel AND p.isActive = true")
    List<Inventory> findLowStockItems();
    
    /**
     * Find all overstock items
     */
    @Query("SELECT i FROM Inventory i JOIN i.product p WHERE i.quantity >= p.maximumStockLevel AND p.isActive = true")
    List<Inventory> findOverstockItems();
    
    /**
     * Find inventory by location
     */
    @Query("SELECT i FROM Inventory i WHERE i.location = :location")
    List<Inventory> findByLocation(@Param("location") String location);
    
    /**
     * Find inventory items that need reordering
     */
    @Query("SELECT i FROM Inventory i JOIN i.product p WHERE i.quantity <= p.reorderPoint AND p.isActive = true")
    List<Inventory> findItemsNeedingReorder();
    
    /**
     * Get total inventory value
     */
    @Query("SELECT SUM(i.quantity * p.cost) FROM Inventory i JOIN i.product p WHERE p.isActive = true")
    Double getTotalInventoryValue();
    
    /**
     * Count products with low stock
     */
    @Query("SELECT COUNT(i) FROM Inventory i JOIN i.product p WHERE i.quantity <= p.minimumStockLevel AND p.isActive = true")
    Long countLowStockItems();
    
    /**
     * Count products with overstock
     */
    @Query("SELECT COUNT(i) FROM Inventory i JOIN i.product p WHERE i.quantity >= p.maximumStockLevel AND p.isActive = true")
    Long countOverstockItems();
}
