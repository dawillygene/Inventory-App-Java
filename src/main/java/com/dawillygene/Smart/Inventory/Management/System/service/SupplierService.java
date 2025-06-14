package com.dawillygene.Smart.Inventory.Management.System.service;

import com.dawillygene.Smart.Inventory.Management.System.entity.Supplier;
import com.dawillygene.Smart.Inventory.Management.System.repository.SupplierRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class SupplierService {
    
    private final SupplierRepository supplierRepository;
    
    public SupplierService(SupplierRepository supplierRepository) {
        this.supplierRepository = supplierRepository;
    }
    
    /**
     * Get all suppliers with pagination
     */
    public Page<Supplier> findAllSuppliers(Pageable pageable) {
        return supplierRepository.findAll(pageable);
    }
    
    /**
     * Get all active suppliers (simplified)
     */
    public List<Supplier> findAllActiveSuppliers() {
        return supplierRepository.findAll();
    }
    
    /**
     * Find supplier by ID
     */
    public Optional<Supplier> findById(Long id) {
        return supplierRepository.findById(id);
    }
    
    /**
     * Find supplier by name
     */
    public Optional<Supplier> findByName(String name) {
        return supplierRepository.findByName(name);
    }
    
    /**
     * Search suppliers (simplified)
     */
    public Page<Supplier> searchSuppliers(String searchTerm, Pageable pageable) {
        return supplierRepository.findAll(pageable);
    }
    
    /**
     * Create new supplier
     */
    public Supplier createSupplier(Supplier supplier) {
        // Check if supplier with the same name already exists
        Optional<Supplier> existingSupplier = supplierRepository.findByName(supplier.getName());
        if (existingSupplier.isPresent()) {
            throw new RuntimeException("Supplier with name '" + supplier.getName() + "' already exists");
        }
        
        // Validate required fields
        validateSupplier(supplier);
        
        // Set default values
        if (supplier.getIsActive() == null) {
            supplier.setIsActive(true);
        }
        
        return supplierRepository.save(supplier);
    }
    
    /**
     * Update existing supplier
     */
    public Supplier updateSupplier(Long id, Supplier supplierDetails) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Supplier not found with ID: " + id));
        
        // Check if another supplier with the same name exists
        Optional<Supplier> existingSupplier = supplierRepository.findByName(supplierDetails.getName());
        if (existingSupplier.isPresent() && !existingSupplier.get().getId().equals(id)) {
            throw new RuntimeException("Another supplier with name '" + supplierDetails.getName() + "' already exists");
        }
        
        // Validate required fields
        validateSupplier(supplierDetails);
        
        // Update fields
        supplier.setName(supplierDetails.getName());
        supplier.setContactPerson(supplierDetails.getContactPerson());
        supplier.setEmail(supplierDetails.getEmail());
        supplier.setAddress(supplierDetails.getAddress());
        supplier.setIsActive(supplierDetails.getIsActive());
        
        return supplierRepository.save(supplier);
    }
    
    /**
     * Delete supplier
     */
    public void deleteSupplier(Long id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Supplier not found with ID: " + id));
        
        // For now, just delete - we'll add product count check later
        supplierRepository.delete(supplier);
    }
    
    /**
     * Get total number of suppliers
     */
    public Long getTotalSupplierCount() {
        return supplierRepository.count();
    }
    
    /**
     * Check if supplier exists by name
     */
    public boolean existsByName(String name) {
        return supplierRepository.findByName(name).isPresent();
    }
    
    /**
     * Validate supplier data
     */
    public void validateSupplier(Supplier supplier) {
        if (supplier.getName() == null || supplier.getName().trim().isEmpty()) {
            throw new RuntimeException("Supplier name is required");
        }
        
        if (supplier.getName().length() > 100) {
            throw new RuntimeException("Supplier name cannot exceed 100 characters");
        }
        
        if (supplier.getContactPerson() == null || supplier.getContactPerson().trim().isEmpty()) {
            throw new RuntimeException("Contact person is required");
        }
        
        if (supplier.getContactPerson().length() > 100) {
            throw new RuntimeException("Contact person name cannot exceed 100 characters");
        }
        
        if (supplier.getEmail() != null && !supplier.getEmail().isEmpty()) {
            if (!isValidEmail(supplier.getEmail())) {
                throw new RuntimeException("Invalid email format");
            }
        }
    }
    
    /**
     * Validate email format
     */
    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }
}
