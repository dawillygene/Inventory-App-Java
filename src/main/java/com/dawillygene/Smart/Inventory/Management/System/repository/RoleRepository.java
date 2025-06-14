package com.dawillygene.Smart.Inventory.Management.System.repository;

import com.dawillygene.Smart.Inventory.Management.System.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for Role entity
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    
    /**
     * Find role by name
     */
    Optional<Role> findByName(String name);
    
    /**
     * Check if role name exists
     */
    boolean existsByName(String name);
}
