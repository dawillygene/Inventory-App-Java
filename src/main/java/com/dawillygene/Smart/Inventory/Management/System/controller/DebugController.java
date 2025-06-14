package com.dawillygene.Smart.Inventory.Management.System.controller;

import com.dawillygene.Smart.Inventory.Management.System.entity.Role;
import com.dawillygene.Smart.Inventory.Management.System.entity.User;
import com.dawillygene.Smart.Inventory.Management.System.repository.RoleRepository;
import com.dawillygene.Smart.Inventory.Management.System.repository.UserRepository;
import com.dawillygene.Smart.Inventory.Management.System.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Temporary debugging controller - REMOVE IN PRODUCTION
 * This controller is only available in development and test environments
 */
@RestController
@RequestMapping("/debug")
@Profile({"dev", "test", "default"}) // Excludes production profile
public class DebugController {

    private static final Logger log = LoggerFactory.getLogger(DebugController.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;

    public DebugController(UserRepository userRepository, RoleRepository roleRepository, 
                          PasswordEncoder passwordEncoder, UserService userService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.userService = userService;
    }

    @GetMapping("/check-admin")
    public Map<String, Object> checkAdminUser() {
        // Find admin user
        Optional<User> adminUser = userRepository.findByUsername("admin");
        
        if (adminUser.isEmpty()) {
            Map<String, Object> response = new java.util.HashMap<>();
            response.put("error", "Admin user does not exist in the database");
            return response;
        }
        
        User user = adminUser.get();
        
        // Get roles
        List<String> roles = user.getRoles().stream()
            .map(Role::getName)
            .collect(Collectors.toList());
            
        // Check if password matches
        boolean passwordMatches = passwordEncoder.matches("admin123", user.getPassword());
            
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("userId", user.getId());
        result.put("username", user.getUsername());
        result.put("email", user.getEmail());
        result.put("isActive", user.getIsActive());
        result.put("roles", roles);
        result.put("passwordLength", user.getPassword().length());
        result.put("passwordMatchesAdmin123", passwordMatches);
        
        return result;
    }
    
    @GetMapping("/roles")
    public List<Map<String, Object>> listAllRoles() {
        return roleRepository.findAll().stream()
            .map(role -> {
                Map<String, Object> map = new java.util.HashMap<>();
                map.put("id", role.getId());
                map.put("name", role.getName());
                map.put("description", role.getDescription() != null ? role.getDescription() : "");
                return map;
            })
            .collect(Collectors.toList());
    }
    
    @PostMapping("/fix-admin")
    public Map<String, String> fixAdminUser() {
        try {
            // Find or create admin user
            Optional<User> existingAdmin = userRepository.findByUsername("admin");
            
            if (existingAdmin.isPresent()) {
                // Update existing admin
                User admin = existingAdmin.get();
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setIsActive(true);
                
                // Ensure admin has ADMIN role
                Role adminRole = roleRepository.findByName(Role.ADMIN)
                    .orElseThrow(() -> new RuntimeException("ADMIN role not found"));
                
                if (admin.getRoles().stream().noneMatch(r -> r.getName().equals(Role.ADMIN))) {
                    admin.getRoles().add(adminRole);
                }
                
                userRepository.save(admin);
                Map<String, String> response = new java.util.HashMap<>();
                response.put("message", "Admin user updated successfully");
                return response;
            } else {
                // Create new admin user
                User newAdmin = new User();
                newAdmin.setUsername("admin");
                newAdmin.setEmail("admin@inventory.com");
                newAdmin.setPassword("admin123");
                newAdmin.setFirstName("System");
                newAdmin.setLastName("Administrator");
                newAdmin.setIsActive(true);
                
                // Get the ADMIN role
                Role adminRole = roleRepository.findByName(Role.ADMIN)
                    .orElseThrow(() -> new RuntimeException("ADMIN role not found"));
                    
                newAdmin.getRoles().add(adminRole);
                
                userService.createUser(newAdmin);
                Map<String, String> response = new java.util.HashMap<>();
                response.put("message", "New admin user created successfully");
                return response;
            }
        } catch (Exception e) {
            log.error("Error in fix-admin endpoint", e);
            Map<String, String> response = new java.util.HashMap<>();
            response.put("error", "Failed to fix admin user: " + e.getMessage());
            return response;
        }
    }
}
