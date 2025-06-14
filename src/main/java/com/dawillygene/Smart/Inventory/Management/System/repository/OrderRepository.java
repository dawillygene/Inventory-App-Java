package com.dawillygene.Smart.Inventory.Management.System.repository;

import com.dawillygene.Smart.Inventory.Management.System.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Order entity
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    /**
     * Find order by order number
     */
    Optional<Order> findByOrderNumber(String orderNumber);
    
    /**
     * Find orders by order type
     */
    @Query("SELECT o FROM Order o WHERE o.orderType = :orderType ORDER BY o.orderDate DESC")
    List<Order> findByOrderType(@Param("orderType") Order.OrderType orderType);
    
    /**
     * Find orders by order status
     */
    @Query("SELECT o FROM Order o WHERE o.orderStatus = :orderStatus ORDER BY o.orderDate DESC")
    List<Order> findByOrderStatus(@Param("orderStatus") Order.OrderStatus orderStatus);
    
    /**
     * Find orders by user
     */
    @Query("SELECT o FROM Order o WHERE o.user.id = :userId ORDER BY o.orderDate DESC")
    List<Order> findByUserId(@Param("userId") Long userId);
    
    /**
     * Find orders by supplier
     */
    @Query("SELECT o FROM Order o WHERE o.supplier.id = :supplierId ORDER BY o.orderDate DESC")
    List<Order> findBySupplierId(@Param("supplierId") Long supplierId);
    
    /**
     * Find orders by date range
     */
    @Query("SELECT o FROM Order o WHERE o.orderDate BETWEEN :startDate AND :endDate ORDER BY o.orderDate DESC")
    List<Order> findByOrderDateBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    /**
     * Find pending orders
     */
    @Query("SELECT o FROM Order o WHERE o.orderStatus = 'PENDING' ORDER BY o.orderDate ASC")
    List<Order> findPendingOrders();
    
    /**
     * Find recent orders (last 30 days)
     */
    @Query("SELECT o FROM Order o WHERE o.orderDate >= :thirtyDaysAgo ORDER BY o.orderDate DESC")
    List<Order> findRecentOrders(@Param("thirtyDaysAgo") LocalDateTime thirtyDaysAgo);
    
    /**
     * Count orders by status
     */
    @Query("SELECT COUNT(o) FROM Order o WHERE o.orderStatus = :orderStatus")
    Long countByOrderStatus(@Param("orderStatus") Order.OrderStatus orderStatus);
    
    /**
     * Count orders by type
     */
    @Query("SELECT COUNT(o) FROM Order o WHERE o.orderType = :orderType")
    Long countByOrderType(@Param("orderType") Order.OrderType orderType);
    
    /**
     * Check if order number exists
     */
    boolean existsByOrderNumber(String orderNumber);
    
    /**
     * Get total sales for a date range
     */
    @Query("SELECT SUM(o.grandTotal) FROM Order o WHERE o.orderType = 'SALE' AND o.orderStatus = 'COMPLETED' " +
           "AND o.orderDate BETWEEN :startDate AND :endDate")
    Double getTotalSalesForPeriod(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    /**
     * Get total purchases for a date range
     */
    @Query("SELECT SUM(o.grandTotal) FROM Order o WHERE o.orderType = 'PURCHASE' AND o.orderStatus = 'COMPLETED' " +
           "AND o.orderDate BETWEEN :startDate AND :endDate")
    Double getTotalPurchasesForPeriod(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}
