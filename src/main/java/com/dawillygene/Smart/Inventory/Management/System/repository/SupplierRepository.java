package com.dawillygene.Smart.Inventory.Management.System.repository;

import com.dawillygene.Smart.Inventory.Management.System.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Supplier entity
 */
@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {
    
    /**
     * Find supplier by name
     */
    Optional<Supplier> findByName(String name);
    
    /**
     * Find supplier by email
     */
    Optional<Supplier> findByEmail(String email);
    
    /**
     * Find all active suppliers
     */
    @Query("SELECT s FROM Supplier s WHERE s.isActive = true ORDER BY s.name")
    List<Supplier> findAllActiveSuppliers();
    
    /**
     * Search suppliers by name or contact person
     */
    @Query("SELECT s FROM Supplier s WHERE (LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(s.contactPerson) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND s.isActive = true")
    List<Supplier> searchByKeyword(@Param("keyword") String keyword);
    
    /**
     * Check if supplier name exists
     */
    boolean existsByName(String name);
    
    /**
     * Check if supplier email exists
     */
    boolean existsByEmail(String email);
    
    /**
     * Find suppliers with products
     */
    @Query("SELECT DISTINCT s FROM Supplier s JOIN s.products p WHERE p.isActive = true AND s.isActive = true")
    List<Supplier> findSuppliersWithActiveProducts();
}
