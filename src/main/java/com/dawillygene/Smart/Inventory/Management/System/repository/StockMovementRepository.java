package com.dawillygene.Smart.Inventory.Management.System.repository;

import com.dawillygene.Smart.Inventory.Management.System.entity.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for StockMovement entity
 */
@Repository
public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {
    
    /**
     * Find stock movements by product ID
     */
    @Query("SELECT sm FROM StockMovement sm WHERE sm.product.id = :productId ORDER BY sm.createdAt DESC")
    List<StockMovement> findByProductId(@Param("productId") Long productId);
    
    /**
     * Find stock movements by movement type
     */
    @Query("SELECT sm FROM StockMovement sm WHERE sm.movementType = :movementType ORDER BY sm.createdAt DESC")
    List<StockMovement> findByMovementType(@Param("movementType") StockMovement.MovementType movementType);
    
    /**
     * Find stock movements by user
     */
    @Query("SELECT sm FROM StockMovement sm WHERE sm.user.id = :userId ORDER BY sm.createdAt DESC")
    List<StockMovement> findByUserId(@Param("userId") Long userId);
    
    /**
     * Find stock movements by order
     */
    @Query("SELECT sm FROM StockMovement sm WHERE sm.order.id = :orderId ORDER BY sm.createdAt DESC")
    List<StockMovement> findByOrderId(@Param("orderId") Long orderId);
    
    /**
     * Find stock movements by date range
     */
    @Query("SELECT sm FROM StockMovement sm WHERE sm.createdAt BETWEEN :startDate AND :endDate ORDER BY sm.createdAt DESC")
    List<StockMovement> findByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    /**
     * Find recent stock movements (last 30 days)
     */
    @Query("SELECT sm FROM StockMovement sm WHERE sm.createdAt >= :thirtyDaysAgo ORDER BY sm.createdAt DESC")
    List<StockMovement> findRecentMovements(@Param("thirtyDaysAgo") LocalDateTime thirtyDaysAgo);
    
    /**
     * Find stock movements by reference number
     */
    @Query("SELECT sm FROM StockMovement sm WHERE sm.referenceNumber = :referenceNumber")
    List<StockMovement> findByReferenceNumber(@Param("referenceNumber") String referenceNumber);
    
    /**
     * Get total stock in for a product
     */
    @Query("SELECT SUM(sm.quantity) FROM StockMovement sm WHERE sm.product.id = :productId " +
           "AND sm.movementType IN ('IN', 'PURCHASE', 'RETURN', 'ADJUSTMENT')")
    Long getTotalStockInForProduct(@Param("productId") Long productId);
    
    /**
     * Get total stock out for a product
     */
    @Query("SELECT SUM(sm.quantity) FROM StockMovement sm WHERE sm.product.id = :productId " +
           "AND sm.movementType IN ('OUT', 'SALE', 'DAMAGE', 'EXPIRED', 'TRANSFER')")
    Long getTotalStockOutForProduct(@Param("productId") Long productId);
    
    /**
     * Count movements by type for today
     */
    @Query("SELECT COUNT(sm) FROM StockMovement sm WHERE sm.movementType = :movementType " +
           "AND DATE(sm.createdAt) = CURRENT_DATE")
    Long countTodayMovementsByType(@Param("movementType") StockMovement.MovementType movementType);
}
