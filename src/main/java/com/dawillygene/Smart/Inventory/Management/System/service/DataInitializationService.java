package com.dawillygene.Smart.Inventory.Management.System.service;

import com.dawillygene.Smart.Inventory.Management.System.entity.Role;
import com.dawillygene.Smart.Inventory.Management.System.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Data initialization service to create default data on application startup
 */
@Component
public class DataInitializationService implements CommandLineRunner {
    
    private static final Logger log = LoggerFactory.getLogger(DataInitializationService.class);
    
    private final RoleService roleService;
    private final UserService userService;
    
    public DataInitializationService(RoleService roleService, UserService userService) {
        this.roleService = roleService;
        this.userService = userService;
    }
    
    @Override
    public void run(String... args) throws Exception {
        log.info("Starting data initialization...");
        
        // Initialize default roles
        initializeRoles();
        
        // Initialize default admin user
        initializeDefaultAdmin();
        
        log.info("Data initialization completed.");
    }
    
    private void initializeRoles() {
        try {
            roleService.initializeDefaultRoles();
            log.info("Default roles initialized successfully.");
        } catch (Exception e) {
            log.error("Error initializing roles: ", e);
        }
    }
    
    private void initializeDefaultAdmin() {
        try {
            // Check if admin user already exists
            if (!userService.existsByUsername("admin")) {
                // Create default admin user
                User adminUser = new User();
                adminUser.setUsername("admin");
                adminUser.setEmail("admin@inventory.com");
                adminUser.setPassword("admin123"); // This will be encoded by UserService
                adminUser.setFirstName("System");
                adminUser.setLastName("Administrator");
                adminUser.setIsActive(true);
                
                // Fetch the ADMIN role and add it to the user BEFORE creating the user
                Role adminRole = roleService.getRoleByName(Role.ADMIN)
                    .orElseThrow(() -> new RuntimeException("ADMIN role not found, cannot create admin user. Make sure roles are initialized."));
                adminUser.getRoles().add(adminRole);
                
                // Create the user (password will be encoded automatically, role is now set)
                User createdAdmin = userService.createUser(adminUser);
                // No need to call addRoleToUser separately if role is added before createUser
                
                log.info("Default admin user created successfully with ADMIN role.");
                log.info("Default admin credentials: username=admin, password=admin123");
            } else {
                log.info("Admin user already exists, skipping creation.");
            }
        } catch (Exception e) {
            log.error("Error creating default admin user: ", e);
        }
    }
}
