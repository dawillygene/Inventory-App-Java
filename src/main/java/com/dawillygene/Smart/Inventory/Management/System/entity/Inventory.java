package com.dawillygene.Smart.Inventory.Management.System.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Inventory entity representing current stock levels for products
 */
@Entity
@Table(name = "inventory")
@EntityListeners(AuditingEntityListener.class)
public class Inventory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity cannot be negative")
    @Column(nullable = false)
    private Integer quantity = 0;
    
    @Column(name = "reserved_quantity")
    private Integer reservedQuantity = 0;
    
    @Column(name = "available_quantity")
    private Integer availableQuantity = 0;
    
    @Column(name = "location")
    private String location;
    
    @Column(name = "bin_number")
    private String binNumber;
    
    @Column(name = "last_counted_date")
    private LocalDateTime lastCountedDate;
    
    @Column(name = "last_restocked_date")
    private LocalDateTime lastRestockedDate;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    
    // Constructors
    public Inventory() {}
    
    public Inventory(Product product, Integer quantity, Integer reservedQuantity, Integer availableQuantity,
                     String location, String binNumber, LocalDateTime lastCountedDate, LocalDateTime lastRestockedDate) {
        this.product = product;
        this.quantity = quantity;
        this.reservedQuantity = reservedQuantity;
        this.availableQuantity = availableQuantity;
        this.location = location;
        this.binNumber = binNumber;
        this.lastCountedDate = lastCountedDate;
        this.lastRestockedDate = lastRestockedDate;
    }

    // Constructor for creating inventory with product and quantity
    public Inventory(Product product, Integer quantity) {
        this.product = product;
        this.quantity = quantity;
        this.availableQuantity = quantity;
    }
    
    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getReservedQuantity() {
        return reservedQuantity;
    }

    public void setReservedQuantity(Integer reservedQuantity) {
        this.reservedQuantity = reservedQuantity;
    }

    public Integer getAvailableQuantity() {
        return availableQuantity;
    }

    public void setAvailableQuantity(Integer availableQuantity) {
        this.availableQuantity = availableQuantity;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getBinNumber() {
        return binNumber;
    }

    public void setBinNumber(String binNumber) {
        this.binNumber = binNumber;
    }

    public LocalDateTime getLastCountedDate() {
        return lastCountedDate;
    }

    public void setLastCountedDate(LocalDateTime lastCountedDate) {
        this.lastCountedDate = lastCountedDate;
    }

    public LocalDateTime getLastRestockedDate() {
        return lastRestockedDate;
    }

    public void setLastRestockedDate(LocalDateTime lastRestockedDate) {
        this.lastRestockedDate = lastRestockedDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    // Helper methods
    public void updateAvailableQuantity() {
        this.availableQuantity = this.quantity - (this.reservedQuantity != null ? this.reservedQuantity : 0);
    }
    
    public void addStock(Integer amount) {
        if (amount != null && amount > 0) {
            this.quantity += amount;
            updateAvailableQuantity();
            this.lastRestockedDate = LocalDateTime.now();
        }
    }
    
    public void removeStock(Integer amount) {
        if (amount != null && amount > 0 && this.quantity >= amount) {
            this.quantity -= amount;
            updateAvailableQuantity();
        }
    }
    
    public void reserveStock(Integer amount) {
        if (amount != null && amount > 0 && this.availableQuantity >= amount) {
            this.reservedQuantity = (this.reservedQuantity != null ? this.reservedQuantity : 0) + amount;
            updateAvailableQuantity();
        }
    }
    
    public void releaseReservedStock(Integer amount) {
        if (amount != null && amount > 0 && this.reservedQuantity >= amount) {
            this.reservedQuantity -= amount;
            updateAvailableQuantity();
        }
    }
    
    public boolean isLowStock() {
        return product != null && quantity <= product.getMinimumStockLevel();
    }
    
    public boolean isOverstock() {
        return product != null && quantity >= product.getMaximumStockLevel();
    }
}
