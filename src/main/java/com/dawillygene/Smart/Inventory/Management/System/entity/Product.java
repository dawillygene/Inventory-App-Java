package com.dawillygene.Smart.Inventory.Management.System.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Product entity representing items in the inventory
 */
@Entity
@Table(name = "products")
@EntityListeners(AuditingEntityListener.class)
public class Product {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Product name is required")
    @Size(max = 200, message = "Product name must not exceed 200 characters")
    @Column(nullable = false)
    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @NotBlank(message = "SKU is required")
    @Size(max = 50, message = "SKU must not exceed 50 characters")
    @Column(unique = true, nullable = false)
    private String sku;
    
    @NotBlank(message = "Barcode is required")
    @Column(unique = true)
    private String barcode;
    
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal price;
    
    @NotNull(message = "Cost is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Cost must be greater than 0")
    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal cost;
    
    @Column(name = "unit_of_measure")
    private String unitOfMeasure = "pcs";
    
    @Column(name = "minimum_stock_level")
    private Integer minimumStockLevel = 0;
    
    @Column(name = "maximum_stock_level")
    private Integer maximumStockLevel = 1000;
    
    @Column(name = "reorder_point")
    private Integer reorderPoint = 10;
    
    @Column(name = "weight_kg")
    private BigDecimal weight;
    
    @Column(name = "dimensions")
    private String dimensions;
    
    @Column(name = "image_url")
    private String imageUrl;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "is_perishable")
    private Boolean isPerishable = false;
    
    @Column(name = "shelf_life_days")
    private Integer shelfLifeDays;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;
    
    @OneToOne(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Inventory inventory;
    
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<StockMovement> stockMovements = new ArrayList<>();
    
    // Constructors
    public Product() {}
    
    public Product(String name, String description, String sku, String barcode, BigDecimal price, BigDecimal cost,
                   String unitOfMeasure, Integer minimumStockLevel, Integer maximumStockLevel, Integer reorderPoint,
                   BigDecimal weight, String dimensions, String imageUrl, Boolean isActive, Boolean isPerishable,
                   Integer shelfLifeDays, Category category, Supplier supplier) {
        this.name = name;
        this.description = description;
        this.sku = sku;
        this.barcode = barcode;
        this.price = price;
        this.cost = cost;
        this.unitOfMeasure = unitOfMeasure;
        this.minimumStockLevel = minimumStockLevel;
        this.maximumStockLevel = maximumStockLevel;
        this.reorderPoint = reorderPoint;
        this.weight = weight;
        this.dimensions = dimensions;
        this.imageUrl = imageUrl;
        this.isActive = isActive;
        this.isPerishable = isPerishable;
        this.shelfLifeDays = shelfLifeDays;
        this.category = category;
        this.supplier = supplier;
    }

    // Constructor for creating product with basic info
    public Product(String name, String sku, String barcode, BigDecimal price, BigDecimal cost) {
        this.name = name;
        this.sku = sku;
        this.barcode = barcode;
        this.price = price;
        this.cost = cost;
    }
    
    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getCost() {
        return cost;
    }

    public void setCost(BigDecimal cost) {
        this.cost = cost;
    }

    public String getUnitOfMeasure() {
        return unitOfMeasure;
    }

    public void setUnitOfMeasure(String unitOfMeasure) {
        this.unitOfMeasure = unitOfMeasure;
    }

    public Integer getMinimumStockLevel() {
        return minimumStockLevel;
    }

    public void setMinimumStockLevel(Integer minimumStockLevel) {
        this.minimumStockLevel = minimumStockLevel;
    }

    public Integer getMaximumStockLevel() {
        return maximumStockLevel;
    }

    public void setMaximumStockLevel(Integer maximumStockLevel) {
        this.maximumStockLevel = maximumStockLevel;
    }

    public Integer getReorderPoint() {
        return reorderPoint;
    }

    public void setReorderPoint(Integer reorderPoint) {
        this.reorderPoint = reorderPoint;
    }

    public BigDecimal getWeight() {
        return weight;
    }

    public void setWeight(BigDecimal weight) {
        this.weight = weight;
    }

    public String getDimensions() {
        return dimensions;
    }

    public void setDimensions(String dimensions) {
        this.dimensions = dimensions;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public boolean isActive() {
        return Boolean.TRUE.equals(isActive);
    }

    public Boolean getIsPerishable() {
        return isPerishable;
    }

    public void setIsPerishable(Boolean isPerishable) {
        this.isPerishable = isPerishable;
    }

    public Integer getShelfLifeDays() {
        return shelfLifeDays;
    }

    public void setShelfLifeDays(Integer shelfLifeDays) {
        this.shelfLifeDays = shelfLifeDays;
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

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Supplier getSupplier() {
        return supplier;
    }

    public void setSupplier(Supplier supplier) {
        this.supplier = supplier;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    public List<StockMovement> getStockMovements() {
        return stockMovements;
    }

    public void setStockMovements(List<StockMovement> stockMovements) {
        this.stockMovements = stockMovements;
    }

    // Helper methods
    public BigDecimal getProfitMargin() {
        if (cost != null && price != null && cost.compareTo(BigDecimal.ZERO) > 0) {
            return price.subtract(cost).divide(cost, 4, java.math.RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }
    
    public Integer getCurrentStock() {
        return inventory != null ? inventory.getQuantity() : 0;
    }
    
    public boolean isLowStock() {
        return getCurrentStock() <= minimumStockLevel;
    }
    
    public boolean isOverstock() {
        return getCurrentStock() >= maximumStockLevel;
    }
}
