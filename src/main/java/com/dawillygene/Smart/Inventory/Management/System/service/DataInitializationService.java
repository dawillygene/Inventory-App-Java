package com.dawillygene.Smart.Inventory.Management.System.service;

import com.dawillygene.Smart.Inventory.Management.System.entity.Role;
import com.dawillygene.Smart.Inventory.Management.System.entity.User;
import com.dawillygene.Smart.Inventory.Management.System.entity.Supplier;
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
    private final SupplierService supplierService;
    
    public DataInitializationService(RoleService roleService, UserService userService, SupplierService supplierService) {
        this.roleService = roleService;
        this.userService = userService;
        this.supplierService = supplierService;
    }
    
    @Override
    public void run(String... args) throws Exception {
        log.info("Starting data initialization...");
        
        // Initialize default roles
        initializeRoles();
        
        // Initialize default admin user
        initializeDefaultAdmin();
        
        // Initialize default suppliers
        initializeDefaultSuppliers();
        
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
                userService.createUser(adminUser);
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
    
    private void initializeDefaultSuppliers() {
        try {
            // Check if suppliers already exist
            if (supplierService.findAllActiveSuppliers().isEmpty()) {
                log.info("No suppliers found, creating default suppliers...");
                
                // Create default suppliers
                Supplier[] defaultSuppliers = {
                    createSupplier("TechCorp Solutions", "John Smith", "john.smith@techcorp.com", 
                                 "+1-555-0101", "123 Tech Avenue, Silicon Valley, CA 94000", 
                                 "https://www.techcorp.com", "TC-987654321", "Net 30", 5,
                                 "Primary technology supplier for electronic components and devices"),
                    
                    createSupplier("Global Materials Ltd", "Sarah Johnson", "sarah.johnson@globalmaterials.com",
                                 "+1-555-0102", "456 Industrial Park, Detroit, MI 48201",
                                 "https://www.globalmaterials.com", "GM-123456789", "Net 45", 7,
                                 "Bulk supplier for raw materials and manufacturing components"),
                    
                    createSupplier("Office Essentials Co", "Mike Chen", "mike.chen@officeessentials.com",
                                 "+1-555-0103", "789 Business District, New York, NY 10001",
                                 "https://www.officeessentials.com", "OE-456789123", "Net 15", 3,
                                 "Office supplies, furniture, and business equipment supplier"),
                    
                    createSupplier("Green Energy Parts", "Emily Davis", "emily.davis@greenenergy.com",
                                 "+1-555-0104", "321 Renewable Way, Austin, TX 73301",
                                 "https://www.greenenergy.com", "GE-789123456", "Net 30", 10,
                                 "Sustainable and renewable energy components supplier"),
                    
                    createSupplier("Auto Components Inc", "Robert Wilson", "robert.wilson@autocomponents.com",
                                 "+1-555-0105", "654 Motor Street, Chicago, IL 60601",
                                 "https://www.autocomponents.com", "AC-321987654", "Net 60", 14,
                                 "Automotive parts and components supplier for all vehicle types")
                };
                
                for (Supplier supplier : defaultSuppliers) {
                    try {
                        supplierService.createSupplier(supplier);
                        log.info("Created supplier: {}", supplier.getName());
                    } catch (Exception e) {
                        log.warn("Supplier '{}' might already exist: {}", supplier.getName(), e.getMessage());
                    }
                }
                
                log.info("Default suppliers initialized successfully.");
            } else {
                log.info("Suppliers already exist, skipping initialization.");
            }
        } catch (Exception e) {
            log.error("Error initializing default suppliers: ", e);
        }
    }
    
    private Supplier createSupplier(String name, String contactPerson, String email, String phoneNumber,
                                  String address, String website, String taxNumber, String paymentTerms,
                                  Integer deliveryTimeDays, String notes) {
        Supplier supplier = new Supplier();
        supplier.setName(name);
        supplier.setContactPerson(contactPerson);
        supplier.setEmail(email);
        supplier.setPhoneNumber(phoneNumber);
        supplier.setAddress(address);
        supplier.setWebsite(website);
        supplier.setTaxNumber(taxNumber);
        supplier.setPaymentTerms(paymentTerms);
        supplier.setDeliveryTimeDays(deliveryTimeDays);
        supplier.setIsActive(true);
        supplier.setNotes(notes);
        return supplier;
    }
}
