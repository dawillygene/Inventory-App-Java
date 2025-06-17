package com.dawillygene.Smart.Inventory.Management.System.service;

import com.dawillygene.Smart.Inventory.Management.System.entity.Order;
import com.dawillygene.Smart.Inventory.Management.System.entity.OrderItem;
import com.dawillygene.Smart.Inventory.Management.System.repository.OrderRepository;
import com.dawillygene.Smart.Inventory.Management.System.repository.OrderItemRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class OrderService {
    
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final InventoryService inventoryService;
    
    public OrderService(OrderRepository orderRepository, OrderItemRepository orderItemRepository, 
                       InventoryService inventoryService) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.inventoryService = inventoryService;
    }
    
    /**
     * Get all orders with pagination
     */
    public Page<Order> findAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable);
    }
    
    /**
     * Find order by ID
     */
    public Optional<Order> findById(Long id) {
        return orderRepository.findById(id);
    }
    
    /**
     * Find orders by status (simplified)
     */
    public List<Order> findOrdersByStatus(Order.OrderStatus status) {
        return orderRepository.findByOrderStatus(status);
    }
    
    /**
     * Find orders by type (simplified)
     */
    public List<Order> findOrdersByType(Order.OrderType type) {
        return orderRepository.findByOrderType(type);
    }
    
    /**
     * Create new purchase order
     */
    public Order createPurchaseOrder(Order order, List<OrderItem> orderItems) {
        // Validate order
        validateOrder(order);
        
        // Set order defaults
        order.setOrderType(Order.OrderType.PURCHASE);
        order.setOrderStatus(Order.OrderStatus.PENDING);
        order.setPaymentStatus(Order.PaymentStatus.PENDING);
        order.setOrderDate(LocalDateTime.now());
        order.setOrderNumber(generateOrderNumber("PO"));
        
        // Calculate total amount
        BigDecimal totalAmount = calculateTotalAmount(orderItems);
        order.setTotalAmount(totalAmount);
        order.setGrandTotal(totalAmount);
        
        // Save order
        Order savedOrder = orderRepository.save(order);
        
        // Save order items
        for (OrderItem item : orderItems) {
            item.setOrder(savedOrder);
            item.setUnitPrice(item.getProduct().getPrice());
            orderItemRepository.save(item);
        }
        
        return savedOrder;
    }
    
    /**
     * Create new sales order
     */
    public Order createSalesOrder(Order order, List<OrderItem> orderItems) {
        // Validate order
        validateOrder(order);
        
        // Check stock availability for all items
        for (OrderItem item : orderItems) {
            if (!inventoryService.hasSufficientStock(item.getProduct().getId(), item.getQuantity())) {
                throw new RuntimeException("Insufficient stock for product: " + item.getProduct().getName());
            }
        }
        
        // Set order defaults
        order.setOrderType(Order.OrderType.SALE);
        order.setOrderStatus(Order.OrderStatus.PENDING);
        order.setPaymentStatus(Order.PaymentStatus.PENDING);
        order.setOrderDate(LocalDateTime.now());
        order.setOrderNumber(generateOrderNumber("SO"));
        
        // Calculate total amount
        BigDecimal totalAmount = calculateTotalAmount(orderItems);
        order.setTotalAmount(totalAmount);
        order.setGrandTotal(totalAmount);
        
        // Save order
        Order savedOrder = orderRepository.save(order);
        
        // Save order items and reserve stock
        for (OrderItem item : orderItems) {
            item.setOrder(savedOrder);
            item.setUnitPrice(item.getProduct().getPrice());
            orderItemRepository.save(item);
            
            // Reserve stock for sales orders
            inventoryService.reserveStock(item.getProduct().getId(), item.getQuantity(), 
                    "Reserved for order: " + savedOrder.getOrderNumber());
        }
        
        return savedOrder;
    }
    
    /**
     * Update order status
     */
    public Order updateOrderStatus(Long orderId, Order.OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));
        
        Order.OrderStatus currentStatus = order.getOrderStatus();
        
        // Validate status transition
        if (!isValidStatusTransition(currentStatus, newStatus)) {
            throw new RuntimeException("Invalid status transition from " + currentStatus + " to " + newStatus);
        }
        
        order.setOrderStatus(newStatus);
        
        // Handle specific status changes
        switch (newStatus) {
            case PENDING:
                // Order is pending - no special action needed
                break;
            case CONFIRMED:
                // Order is confirmed - reserve stock for sales orders
                if (order.getOrderType() == Order.OrderType.SALE) {
                    List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);
                    for (OrderItem item : orderItems) {
                        inventoryService.reserveStock(item.getProduct().getId(), item.getQuantity(),
                                "Stock reserved for order: " + order.getOrderNumber());
                    }
                }
                break;
            case PROCESSING:
                // Order is being processed - no special action needed
                break;
            case SHIPPED:
                // Order is shipped - no special action needed
                break;
            case DELIVERED:
                order.setActualDeliveryDate(LocalDateTime.now());
                if (order.getOrderType() == Order.OrderType.PURCHASE) {
                    // Update inventory for purchase orders
                    List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);
                    for (OrderItem item : orderItems) {
                        inventoryService.addStock(item.getProduct().getId(), item.getQuantity(),
                                "Purchase order delivered: " + order.getOrderNumber());
                    }
                } else if (order.getOrderType() == Order.OrderType.SALE) {
                    // Remove reserved stock for sales orders
                    List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);
                    for (OrderItem item : orderItems) {
                        inventoryService.removeStock(item.getProduct().getId(), item.getQuantity(),
                                "Sales order delivered: " + order.getOrderNumber());
                    }
                }
                break;
            case COMPLETED:
                // Order is completed - same as delivered
                if (order.getActualDeliveryDate() == null) {
                    order.setActualDeliveryDate(LocalDateTime.now());
                }
                break;
            case CANCELLED:
                if (order.getOrderType() == Order.OrderType.SALE) {
                    // Release reserved stock for cancelled sales orders
                    List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);
                    for (OrderItem item : orderItems) {
                        inventoryService.releaseReservedStock(item.getProduct().getId(), item.getQuantity(),
                                "Order cancelled: " + order.getOrderNumber());
                    }
                }
                break;
            case RETURNED:
                // Handle returned orders - reverse inventory changes
                if (order.getOrderType() == Order.OrderType.SALE) {
                    List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);
                    for (OrderItem item : orderItems) {
                        inventoryService.addStock(item.getProduct().getId(), item.getQuantity(),
                                "Sales order returned: " + order.getOrderNumber());
                    }
                } else if (order.getOrderType() == Order.OrderType.PURCHASE) {
                    List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);
                    for (OrderItem item : orderItems) {
                        inventoryService.removeStock(item.getProduct().getId(), item.getQuantity(),
                                "Purchase order returned: " + order.getOrderNumber());
                    }
                }
                break;
        }
        
        return orderRepository.save(order);
    }
    
    /**
     * Update payment status
     */
    public Order updatePaymentStatus(Long orderId, Order.PaymentStatus newPaymentStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));
        
        order.setPaymentStatus(newPaymentStatus);
        
        return orderRepository.save(order);
    }
    
    /**
     * Get order items for an order
     */
    public List<OrderItem> getOrderItems(Long orderId) {
        return orderItemRepository.findByOrderId(orderId);
    }
    
    /**
     * Delete order (only if pending)
     */
    public void deleteOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));
        
        if (order.getOrderStatus() != Order.OrderStatus.PENDING) {
            throw new RuntimeException("Cannot delete order with status: " + order.getOrderStatus());
        }
        
        // Release reserved stock for sales orders
        if (order.getOrderType() == Order.OrderType.SALE) {
            List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);
            for (OrderItem item : orderItems) {
                inventoryService.releaseReservedStock(item.getProduct().getId(), item.getQuantity(),
                        "Order deleted: " + order.getOrderNumber());
            }
        }
        
        // Delete order items first (if not cascade delete)
        List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);
        orderItemRepository.deleteAll(orderItems);
        
        // Delete order
        orderRepository.delete(order);
    }
    
    /**
     * Get order statistics (simplified)
     */
    public OrderStats getOrderStats() {
        Long totalOrders = orderRepository.count();
        Long pendingOrders = (long) orderRepository.findByOrderStatus(Order.OrderStatus.PENDING).size();
        Long confirmedOrders = (long) orderRepository.findByOrderStatus(Order.OrderStatus.CONFIRMED).size();
        Long deliveredOrders = (long) orderRepository.findByOrderStatus(Order.OrderStatus.DELIVERED).size();
        Long cancelledOrders = (long) orderRepository.findByOrderStatus(Order.OrderStatus.CANCELLED).size();
        
        BigDecimal totalSalesAmount = BigDecimal.ZERO; // Will implement proper calculation later
        BigDecimal totalPurchaseAmount = BigDecimal.ZERO; // Will implement proper calculation later
        
        return new OrderStats(totalOrders, pendingOrders, confirmedOrders, deliveredOrders, 
                cancelledOrders, totalSalesAmount, totalPurchaseAmount);
    }
    
    /**
     * Generate order number
     */
    private String generateOrderNumber(String prefix) {
        long count = orderRepository.count() + 1;
        return prefix + "-" + String.format("%06d", count);
    }
    
    /**
     * Calculate total amount for order items
     */
    private BigDecimal calculateTotalAmount(List<OrderItem> orderItems) {
        return orderItems.stream()
                .map(item -> item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    /**
     * Validate order data
     */
    private void validateOrder(Order order) {
        if (order.getSupplier() == null && order.getOrderType() == Order.OrderType.PURCHASE) {
            throw new RuntimeException("Supplier is required for purchase orders");
        }
        
        if (order.getCustomerName() == null && order.getOrderType() == Order.OrderType.SALE) {
            throw new RuntimeException("Customer name is required for sales orders");
        }
    }
    
    /**
     * Check if status transition is valid
     */
    private boolean isValidStatusTransition(Order.OrderStatus currentStatus, Order.OrderStatus newStatus) {
        return switch (currentStatus) {
            case PENDING -> newStatus == Order.OrderStatus.CONFIRMED || newStatus == Order.OrderStatus.CANCELLED;
            case CONFIRMED -> newStatus == Order.OrderStatus.DELIVERED || newStatus == Order.OrderStatus.CANCELLED;
            case DELIVERED -> false; // Final state
            case CANCELLED -> false; // Final state
            default -> true; // Allow other transitions for now
        };
    }
    
    /**
     * Inner class for order statistics
     */
    public static class OrderStats {
        private final Long totalOrders;
        private final Long pendingOrders;
        private final Long confirmedOrders;
        private final Long deliveredOrders;
        private final Long cancelledOrders;
        private final BigDecimal totalSalesAmount;
        private final BigDecimal totalPurchaseAmount;
        
        public OrderStats(Long totalOrders, Long pendingOrders, Long confirmedOrders, 
                         Long deliveredOrders, Long cancelledOrders, 
                         BigDecimal totalSalesAmount, BigDecimal totalPurchaseAmount) {
            this.totalOrders = totalOrders;
            this.pendingOrders = pendingOrders;
            this.confirmedOrders = confirmedOrders;
            this.deliveredOrders = deliveredOrders;
            this.cancelledOrders = cancelledOrders;
            this.totalSalesAmount = totalSalesAmount != null ? totalSalesAmount : BigDecimal.ZERO;
            this.totalPurchaseAmount = totalPurchaseAmount != null ? totalPurchaseAmount : BigDecimal.ZERO;
        }
        
        // Getters
        public Long getTotalOrders() { return totalOrders; }
        public Long getPendingOrders() { return pendingOrders; }
        public Long getConfirmedOrders() { return confirmedOrders; }
        public Long getDeliveredOrders() { return deliveredOrders; }
        public Long getCancelledOrders() { return cancelledOrders; }
        public BigDecimal getTotalSalesAmount() { return totalSalesAmount; }
        public BigDecimal getTotalPurchaseAmount() { return totalPurchaseAmount; }
    }

    /**
     * Get total order count
     */
    public long getTotalOrderCount() {
        return orderRepository.count();
    }
    
    /**
     * Get orders count since a specific date
     */
    public long getOrdersCountSince(LocalDateTime since) {
        return orderRepository.countByOrderDateAfter(since);
    }
    
    /**
     * Find sales orders by date range
     */
    public List<Order> findSalesOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return orderRepository.findByOrderTypeAndOrderDateBetween(Order.OrderType.SALE, startDate, endDate);
    }
    
    /**
     * Find purchase orders by date range
     */
    public List<Order> findPurchaseOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate, Long supplierId) {
        if (supplierId != null) {
            return orderRepository.findByOrderTypeAndOrderDateBetweenAndSupplierId(Order.OrderType.PURCHASE, startDate, endDate, supplierId);
        } else {
            return orderRepository.findByOrderTypeAndOrderDateBetween(Order.OrderType.PURCHASE, startDate, endDate);
        }
    }
    
    /**
     * Find orders by supplier
     */
    public List<Order> findOrdersBySupplier(Long supplierId) {
        return orderRepository.findBySupplierId(supplierId);
    }
    
    /**
     * Get orders count for a specific month
     */
    public long getOrdersCountForMonth(int year, int month) {
        return orderRepository.countByYearAndMonth(year, month);
    }
    
    /**
     * Get orders count by supplier
     */
    public long getOrdersCountBySupplier(Long supplierId) {
        return orderRepository.countBySupplierId(supplierId);
    }
}
