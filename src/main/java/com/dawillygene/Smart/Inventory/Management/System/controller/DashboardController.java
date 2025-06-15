package com.dawillygene.Smart.Inventory.Management.System.controller;

import com.dawillygene.Smart.Inventory.Management.System.service.ProductService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller for the main dashboard
 */
@Controller
@RequestMapping("/dashboard")
public class DashboardController {
    
    private final ProductService productService;
    
    public DashboardController(ProductService productService) {
        this.productService = productService;
    }
    
    /**
     * Main dashboard page
     */
    @GetMapping
    public String dashboard(Model model, Authentication authentication) {
        // Get dashboard metrics
        long totalProducts = productService.getAllActiveProducts().size();
        long lowStockProducts = productService.getProductsWithLowStock().size();
        long totalSuppliers = 0; // Will be implemented when SupplierService is created
        long pendingOrders = 0; // Will be implemented when OrderService is created
        
        // Add metrics to model
        model.addAttribute("totalProducts", totalProducts);
        model.addAttribute("lowStockProducts", lowStockProducts);
        model.addAttribute("totalSuppliers", totalSuppliers);
        model.addAttribute("pendingOrders", pendingOrders);
        
        // Get recent products (top 5)
        model.addAttribute("recentProducts", productService.getAllActiveProducts().stream().limit(5).toList());
        
        // Add user info
        model.addAttribute("currentUser", authentication.getName());
        
        // Add current URI for navigation highlighting
        model.addAttribute("currentUri", "/dashboard");
        
        return "dashboard/index";
    }
}
