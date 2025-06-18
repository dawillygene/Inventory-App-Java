package com.dawillygene.Smart.Inventory.Management.System.service;

import com.dawillygene.Smart.Inventory.Management.System.entity.Inventory;
import com.dawillygene.Smart.Inventory.Management.System.entity.Product;
import com.dawillygene.Smart.Inventory.Management.System.entity.StockMovement;
import com.dawillygene.Smart.Inventory.Management.System.repository.InventoryRepository;
import com.dawillygene.Smart.Inventory.Management.System.repository.StockMovementRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class InventoryService {
    
    private final InventoryRepository inventoryRepository;
    private final StockMovementRepository stockMovementRepository;
    
    public InventoryService(InventoryRepository inventoryRepository, StockMovementRepository stockMovementRepository) {
        this.inventoryRepository = inventoryRepository;
        this.stockMovementRepository = stockMovementRepository;
    }
    
    /**
     * Get all inventory items with pagination
     */
    public Page<Inventory> findAllInventory(Pageable pageable) {
        return inventoryRepository.findAll(pageable);
    }
    
    /**
     * Find inventory by product ID
     */
    public Optional<Inventory> findInventoryByProduct(Long productId) {
        return inventoryRepository.findByProductId(productId);
    }
    
    /**
     * Get low stock inventory items
     */
    public List<Inventory> findLowStockItems() {
        return inventoryRepository.findLowStockItems();
    }
    
    /**
     * Search inventory by product name
     */
    public Page<Inventory> searchInventoryByProductName(String productName, Pageable pageable) {
        // This will need to be implemented as a custom query in the repository
        return inventoryRepository.findAll(pageable); // Temporary - will fix after adding repository method
    }
    
    /**
     * Update stock quantity and create stock movement record
     */
    public Inventory updateStock(Long productId, Integer quantityChange, String movementType, String notes) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new RuntimeException("Inventory not found for product ID: " + productId));
        
        // Calculate new quantity
        Integer newQuantity = inventory.getQuantity() + quantityChange;
        if (newQuantity < 0) {
            throw new RuntimeException("Insufficient stock. Current quantity: " + inventory.getQuantity());
        }
        
        // Update inventory
        inventory.setQuantity(newQuantity);
        inventory.setUpdatedAt(LocalDateTime.now());
        
        // Create stock movement record
        StockMovement stockMovement = new StockMovement();
        stockMovement.setProduct(inventory.getProduct());
        stockMovement.setMovementType(StockMovement.MovementType.valueOf(movementType.toUpperCase()));
        stockMovement.setQuantity(Math.abs(quantityChange));
        stockMovement.setPreviousQuantity(inventory.getQuantity() - quantityChange);
        stockMovement.setNewQuantity(newQuantity);
        stockMovement.setReferenceNumber(generateReferenceNumber());
        stockMovement.setNotes(notes);
        stockMovement.setCreatedAt(LocalDateTime.now());
        
        // Save both records
        stockMovementRepository.save(stockMovement);
        return inventoryRepository.save(inventory);
    }
    
    /**
     * Add stock (incoming inventory)
     */
    public Inventory addStock(Long productId, Integer quantity, String notes) {
        return updateStock(productId, quantity, "IN", notes);
    }
    
    /**
     * Remove stock (outgoing inventory)
     */
    public Inventory removeStock(Long productId, Integer quantity, String notes) {
        return updateStock(productId, -quantity, "OUT", notes);
    }
    
    /**
     * Adjust stock to specific quantity
     */
    public Inventory adjustStockTo(Long productId, Integer targetQuantity, String notes) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new RuntimeException("Inventory not found for product ID: " + productId));
        
        Integer currentQuantity = inventory.getQuantity();
        Integer difference = targetQuantity - currentQuantity;
        
        String movementType = difference >= 0 ? "IN" : "OUT"; // Use existing types
        return updateStock(productId, difference, movementType, notes);
    }
    
    /**
     * Create new inventory record for a product
     */
    public Inventory createInventory(Product product, Integer initialQuantity, Integer minStockLevel, String location) {
        // Check if inventory already exists for this product
        Optional<Inventory> existingInventory = inventoryRepository.findByProductId(product.getId());
        if (existingInventory.isPresent()) {
            throw new RuntimeException("Inventory already exists for product: " + product.getName());
        }
        
        Inventory inventory = new Inventory();
        inventory.setProduct(product);
        inventory.setQuantity(initialQuantity);
        inventory.setLocation(location);
        inventory.setUpdatedAt(LocalDateTime.now());
        
        Inventory savedInventory = inventoryRepository.save(inventory);
        
        // Create initial stock movement record if quantity > 0
        if (initialQuantity > 0) {
            StockMovement stockMovement = new StockMovement();
            stockMovement.setProduct(product);
            stockMovement.setMovementType(StockMovement.MovementType.IN); // Use existing IN type
            stockMovement.setQuantity(initialQuantity);
            stockMovement.setPreviousQuantity(0);
            stockMovement.setNewQuantity(initialQuantity);
            stockMovement.setReferenceNumber(generateReferenceNumber());
            stockMovement.setNotes("Initial inventory setup");
            stockMovement.setCreatedAt(LocalDateTime.now());
            stockMovementRepository.save(stockMovement);
        }
        
        return savedInventory;
    }
    
    /**
     * Update inventory details (location, etc.)
     */
    public Inventory updateInventoryDetails(Long inventoryId, String location) {
        Inventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new RuntimeException("Inventory not found with ID: " + inventoryId));
        
        inventory.setLocation(location);
        inventory.setUpdatedAt(LocalDateTime.now());
        
        return inventoryRepository.save(inventory);
    }
    
    /**
     * Get stock movement history for a product
     */
    public Page<StockMovement> getStockMovementHistory(Long productId, Pageable pageable) {
        // Temporary implementation - will add repository method later
        return stockMovementRepository.findAll(pageable);
    }
    
    /**
     * Get recent stock movements across all products
     */
    public Page<StockMovement> getRecentStockMovements(Pageable pageable) {
        // Temporary implementation - will add repository method later
        return stockMovementRepository.findAll(pageable);
    }
    
    /**
     * Calculate total inventory value
     */
    public BigDecimal calculateTotalInventoryValue() {
        // Temporary implementation - will add repository method later
        return BigDecimal.ZERO;
    }
    
    /**
     * Get low stock count
     */
    public Long getLowStockCount() {
        return inventoryRepository.countLowStockItems();
    }
    
    /**
     * Get out of stock count
     */
    public Long getOutOfStockCount() {
        // Temporary implementation - will add repository method later
        return inventoryRepository.count(); // Placeholder
    }
    
    /**
     * Delete inventory record
     */
    public void deleteInventory(Long inventoryId) {
        Inventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new RuntimeException("Inventory not found with ID: " + inventoryId));
        
        // Check if there are any stock movements
        // Temporary implementation - will add repository method later
        Long movementCount = 0L; // stockMovementRepository.countByProductId(inventory.getProduct().getId());
        if (movementCount > 0) {
            throw new RuntimeException("Cannot delete inventory with existing stock movements. Product: " + 
                    inventory.getProduct().getName());
        }
        
        inventoryRepository.delete(inventory);
    }
    
    /**
     * Check if product has sufficient stock
     */
    public boolean hasSufficientStock(Long productId, Integer requiredQuantity) {
        Optional<Inventory> inventory = inventoryRepository.findByProductId(productId);
        return inventory.map(inv -> inv.getQuantity() >= requiredQuantity).orElse(false);
    }
    
    /**
     * Reserve stock for orders
     */
    public Inventory reserveStock(Long productId, Integer quantity, String notes) {
        return updateStock(productId, -quantity, "OUT", notes); // Use existing OUT type
    }
    
    /**
     * Release reserved stock
     */
    public Inventory releaseReservedStock(Long productId, Integer quantity, String notes) {
        return updateStock(productId, quantity, "IN", notes); // Use existing IN type
    }
    
    /**
     * Get all inventory items (non-paginated)
     */
    public List<Inventory> getAllInventory() {
        return inventoryRepository.findAll();
    }
    
    /**
     * Get inventory by ID
     */
    public Inventory getInventoryById(Long id) {
        return inventoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Inventory not found with ID: " + id));
    }
    
    /**
     * Adjust stock with inventory ID instead of product ID
     */
    public Inventory adjustStock(Long inventoryId, Integer newQuantity, String reason) {
        Inventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new RuntimeException("Inventory not found with ID: " + inventoryId));
        
        Integer currentQuantity = inventory.getQuantity();
        Integer difference = newQuantity - currentQuantity;
        
        if (difference == 0) {
            return inventory; // No change needed
        }
        
        // Update inventory quantity
        inventory.setQuantity(newQuantity);
        inventory.setUpdatedAt(LocalDateTime.now());
        
        // Create stock movement record
        StockMovement stockMovement = new StockMovement();
        stockMovement.setProduct(inventory.getProduct());
        stockMovement.setMovementType(StockMovement.MovementType.ADJUSTMENT);
        stockMovement.setQuantity(Math.abs(difference));
        stockMovement.setPreviousQuantity(currentQuantity);
        stockMovement.setNewQuantity(newQuantity);
        stockMovement.setReferenceNumber(generateReferenceNumber());
        stockMovement.setReason(reason);
        stockMovement.setCreatedAt(LocalDateTime.now());
        
        // Save both records
        stockMovementRepository.save(stockMovement);
        return inventoryRepository.save(inventory);
    }
    
    /**
     * Get low stock products for reports
     */
    public List<Product> getLowStockProducts() {
        return inventoryRepository.findLowStockItems().stream()
                .map(Inventory::getProduct)
                .toList();
    }
    
    /**
     * Get current stock value for all inventory
     */
    public BigDecimal getCurrentStockValue() {
        Double value = inventoryRepository.getTotalInventoryValue();
        return value != null ? BigDecimal.valueOf(value) : BigDecimal.ZERO;
    }
    
    /**
     * Get total stock value (alias for getCurrentStockValue)
     */
    public BigDecimal getTotalStockValue() {
        return getCurrentStockValue();
    }
    
    /**
     * Get product stock level by product ID
     */
    public Integer getProductStockLevel(Long productId) {
        Optional<Inventory> inventory = inventoryRepository.findByProductId(productId);
        return inventory.map(Inventory::getQuantity).orElse(0);
    }
    
    /**
     * Generate a unique reference number for stock movements
     */
    private String generateReferenceNumber() {
        LocalDateTime now = LocalDateTime.now();
        String timestamp = now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        // Add a random suffix to ensure uniqueness in case of concurrent operations
        long randomSuffix = (long) (Math.random() * 1000);
        return "SM" + timestamp + String.format("%03d", randomSuffix);
    }
}
