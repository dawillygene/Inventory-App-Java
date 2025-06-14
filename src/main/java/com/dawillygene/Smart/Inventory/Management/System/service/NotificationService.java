package com.dawillygene.Smart.Inventory.Management.System.service;

import com.dawillygene.Smart.Inventory.Management.System.entity.Inventory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
@Transactional
public class NotificationService {
    
    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    
    private final InventoryService inventoryService;
    
    public NotificationService(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }
    
    // In-memory notification storage (in a real application, this would be in the database)
    private final ConcurrentHashMap<Long, ConcurrentLinkedQueue<Notification>> userNotifications = new ConcurrentHashMap<>();
    private final ConcurrentLinkedQueue<Notification> systemNotifications = new ConcurrentLinkedQueue<>();
    
    /**
     * Send notification to a specific user
     */
    public void sendNotificationToUser(Long userId, String title, String message, NotificationType type) {
        Notification notification = new Notification(
                generateNotificationId(),
                title,
                message,
                type,
                LocalDateTime.now(),
                false,
                userId
        );
        
        userNotifications.computeIfAbsent(userId, k -> new ConcurrentLinkedQueue<>()).offer(notification);
        log.info("Notification sent to user {}: {}", userId, title);
    }
    
    /**
     * Send notification to all admin users
     */
    public void sendNotificationToAdmins(String title, String message, NotificationType type) {
        // Temporary implementation - will fix after UserService method is available
        // List<User> adminUsers = userService.findUsersByRole("ADMIN");
        // for (User admin : adminUsers) {
        //     sendNotificationToUser(admin.getId(), title, message, type);
        // }
        log.info("Admin notification: {} - {}", title, message);
    }
    
    /**
     * Send system-wide notification
     */
    public void sendSystemNotification(String title, String message, NotificationType type) {
        Notification notification = new Notification(
                generateNotificationId(),
                title,
                message,
                type,
                LocalDateTime.now(),
                false,
                null
        );
        
        systemNotifications.offer(notification);
        log.info("System notification sent: {}", title);
    }
    
    /**
     * Get unread notifications for a user
     */
    public List<Notification> getUnreadNotifications(Long userId) {
        ConcurrentLinkedQueue<Notification> notifications = userNotifications.get(userId);
        if (notifications == null) {
            return new ArrayList<>();
        }
        
        return notifications.stream()
                .filter(n -> !n.isRead())
                .toList();
    }
    
    /**
     * Get all notifications for a user
     */
    public List<Notification> getAllNotifications(Long userId) {
        ConcurrentLinkedQueue<Notification> notifications = userNotifications.get(userId);
        if (notifications == null) {
            return new ArrayList<>();
        }
        
        return new ArrayList<>(notifications);
    }
    
    /**
     * Mark notification as read
     */
    public void markAsRead(Long notificationId, Long userId) {
        ConcurrentLinkedQueue<Notification> notifications = userNotifications.get(userId);
        if (notifications != null) {
            notifications.stream()
                    .filter(n -> n.getId().equals(notificationId))
                    .findFirst()
                    .ifPresent(n -> n.setRead(true));
        }
    }
    
    /**
     * Mark all notifications as read for a user
     */
    public void markAllAsRead(Long userId) {
        ConcurrentLinkedQueue<Notification> notifications = userNotifications.get(userId);
        if (notifications != null) {
            notifications.forEach(n -> n.setRead(true));
        }
    }
    
    /**
     * Delete notification
     */
    public void deleteNotification(Long notificationId, Long userId) {
        ConcurrentLinkedQueue<Notification> notifications = userNotifications.get(userId);
        if (notifications != null) {
            notifications.removeIf(n -> n.getId().equals(notificationId));
        }
    }
    
    /**
     * Get count of unread notifications for a user
     */
    public Long getUnreadNotificationCount(Long userId) {
        return (long) getUnreadNotifications(userId).size();
    }
    
    /**
     * Check for low stock and send notifications
     */
    public void checkLowStockAndNotify() {
        try {
            List<Inventory> lowStockItems = inventoryService.findLowStockItems();
            
            if (!lowStockItems.isEmpty()) {
                StringBuilder message = new StringBuilder("The following items are running low on stock:\n");
                
                for (Inventory item : lowStockItems) {
                    message.append("- ").append(item.getProduct().getName())
                           .append(" (Current: ").append(item.getQuantity())
                           .append(")\n");
                }
                
                sendNotificationToAdmins(
                        "Low Stock Alert", 
                        message.toString(), 
                        NotificationType.WARNING
                );
                
                log.info("Low stock notifications sent for {} items", lowStockItems.size());
            }
        } catch (Exception e) {
            log.error("Error checking low stock: ", e);
        }
    }
    
    /**
     * Check for out of stock and send notifications
     */
    public void checkOutOfStockAndNotify() {
        try {
            Long outOfStockCount = inventoryService.getOutOfStockCount();
            
            if (outOfStockCount > 0) {
                sendNotificationToAdmins(
                        "Out of Stock Alert",
                        "There are " + outOfStockCount + " products that are completely out of stock.",
                        NotificationType.ERROR
                );
                
                log.info("Out of stock notification sent for {} items", outOfStockCount);
            }
        } catch (Exception e) {
            log.error("Error checking out of stock: ", e);
        }
    }
    
    /**
     * Send order status notification
     */
    public void sendOrderStatusNotification(Long orderId, String orderNumber, String status, String customerName) {
        String message = String.format("Order %s has been %s", orderNumber, status.toLowerCase());
        if (customerName != null) {
            message += " for customer: " + customerName;
        }
        
        sendNotificationToAdmins(
                "Order Status Update",
                message,
                NotificationType.INFO
        );
    }
    
    /**
     * Send new order notification
     */
    public void sendNewOrderNotification(String orderNumber, String orderType, String amount) {
        String message = String.format("New %s order %s has been created with total amount: %s", 
                orderType.toLowerCase(), orderNumber, amount);
        
        sendNotificationToAdmins(
                "New Order Created",
                message,
                NotificationType.INFO
        );
    }
    
    /**
     * Send payment notification
     */
    public void sendPaymentNotification(String orderNumber, String paymentStatus, String amount) {
        String message = String.format("Payment for order %s has been %s. Amount: %s", 
                orderNumber, paymentStatus.toLowerCase(), amount);
        
        sendNotificationToAdmins(
                "Payment Update",
                message,
                NotificationType.INFO
        );
    }
    
    /**
     * Send user registration notification
     */
    public void sendUserRegistrationNotification(String username, String email) {
        String message = String.format("New user registered: %s (%s)", username, email);
        
        sendNotificationToAdmins(
                "New User Registration",
                message,
                NotificationType.INFO
        );
    }
    
    /**
     * Send system error notification
     */
    public void sendSystemErrorNotification(String errorMessage, String source) {
        String message = String.format("System error in %s: %s", source, errorMessage);
        
        sendNotificationToAdmins(
                "System Error",
                message,
                NotificationType.ERROR
        );
    }
    
    /**
     * Clear old notifications (keep only last 100 per user)
     */
    public void clearOldNotifications() {
        userNotifications.forEach((userId, notifications) -> {
            if (notifications.size() > 100) {
                int toRemove = notifications.size() - 100;
                for (int i = 0; i < toRemove; i++) {
                    notifications.poll();
                }
            }
        });
        
        // Clear old system notifications
        if (systemNotifications.size() > 100) {
            int toRemove = systemNotifications.size() - 100;
            for (int i = 0; i < toRemove; i++) {
                systemNotifications.poll();
            }
        }
        
        log.info("Old notifications cleared");
    }
    
    /**
     * Generate unique notification ID
     */
    private Long generateNotificationId() {
        return System.currentTimeMillis() + (long) (Math.random() * 1000);
    }
    
    /**
     * Notification types enum
     */
    public enum NotificationType {
        INFO,
        SUCCESS,
        WARNING,
        ERROR
    }
    
    /**
     * Notification class
     */
    public static class Notification {
        private Long id;
        private String title;
        private String message;
        private NotificationType type;
        private LocalDateTime createdAt;
        private boolean read;
        private Long userId; // null for system notifications
        
        public Notification(Long id, String title, String message, NotificationType type, 
                          LocalDateTime createdAt, boolean read, Long userId) {
            this.id = id;
            this.title = title;
            this.message = message;
            this.type = type;
            this.createdAt = createdAt;
            this.read = read;
            this.userId = userId;
        }
        
        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public NotificationType getType() { return type; }
        public void setType(NotificationType type) { this.type = type; }
        
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
        
        public boolean isRead() { return read; }
        public void setRead(boolean read) { this.read = read; }
        
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
    }
}
