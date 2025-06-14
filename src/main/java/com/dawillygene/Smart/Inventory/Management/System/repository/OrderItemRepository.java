package com.dawillygene.Smart.Inventory.Management.System.repository;

import com.dawillygene.Smart.Inventory.Management.System.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for OrderItem entity
 */
@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    
    /**
     * Find order items by order ID
     */
    @Query("SELECT oi FROM OrderItem oi WHERE oi.order.id = :orderId")
    List<OrderItem> findByOrderId(@Param("orderId") Long orderId);
    
    /**
     * Find order items by product ID
     */
    @Query("SELECT oi FROM OrderItem oi WHERE oi.product.id = :productId")
    List<OrderItem> findByProductId(@Param("productId") Long productId);
    
    /**
     * Get total quantity sold for a product
     */
    @Query("SELECT SUM(oi.quantity) FROM OrderItem oi JOIN oi.order o WHERE oi.product.id = :productId " +
           "AND o.orderType = 'SALE' AND o.orderStatus = 'COMPLETED'")
    Long getTotalQuantitySoldForProduct(@Param("productId") Long productId);
    
    /**
     * Get total quantity purchased for a product
     */
    @Query("SELECT SUM(oi.quantity) FROM OrderItem oi JOIN oi.order o WHERE oi.product.id = :productId " +
           "AND o.orderType = 'PURCHASE' AND o.orderStatus = 'COMPLETED'")
    Long getTotalQuantityPurchasedForProduct(@Param("productId") Long productId);
    
    /**
     * Find top selling products
     */
    @Query("SELECT oi.product, SUM(oi.quantity) as totalSold FROM OrderItem oi JOIN oi.order o " +
           "WHERE o.orderType = 'SALE' AND o.orderStatus = 'COMPLETED' " +
           "GROUP BY oi.product ORDER BY totalSold DESC")
    List<Object[]> findTopSellingProducts();
}
