package com.dawillygene.Smart.Inventory.Management.System.service;

import com.dawillygene.Smart.Inventory.Management.System.entity.Role;
import com.dawillygene.Smart.Inventory.Management.System.repository.RoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service class for Role management operations
 */
@Service
@Transactional
public class RoleService {
    
    private final RoleRepository roleRepository;
    
    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }
    
    /**
     * Create a new role
     */
    public Role createRole(Role role) {
        return roleRepository.save(role);
    }
    
    /**
     * Get role by ID
     */
    @Transactional(readOnly = true)
    public Role getRoleById(Long roleId) {
        return roleRepository.findById(roleId)
            .orElseThrow(() -> new RuntimeException("Role not found with id: " + roleId));
    }
    
    /**
     * Get role by name
     */
    @Transactional(readOnly = true)
    public Optional<Role> getRoleByName(String name) {
        return roleRepository.findByName(name);
    }
    
    /**
     * Get all roles
     */
    @Transactional(readOnly = true)
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }
    
    /**
     * Update role
     */
    public Role updateRole(Long roleId, Role roleDetails) {
        Role existingRole = getRoleById(roleId);
        existingRole.setName(roleDetails.getName());
        existingRole.setDescription(roleDetails.getDescription());
        return roleRepository.save(existingRole);
    }
    
    /**
     * Delete role
     */
    public void deleteRole(Long roleId) {
        Role role = getRoleById(roleId);
        roleRepository.delete(role);
    }
    
    /**
     * Check if role exists by name
     */
    @Transactional(readOnly = true)
    public boolean existsByName(String name) {
        return roleRepository.existsByName(name);
    }
    
    /**
     * Initialize default roles
     */
    public void initializeDefaultRoles() {
        if (!existsByName(Role.ADMIN)) {
            Role adminRole = new Role(Role.ADMIN, "Administrator with full system access");
            createRole(adminRole);
        }
        
        if (!existsByName(Role.STAFF)) {
            Role staffRole = new Role(Role.STAFF, "Staff member with limited system access");
            createRole(staffRole);
        }
    }
}
