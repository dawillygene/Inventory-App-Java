package com.dawillygene.Smart.Inventory.Management.System.controller;

import com.dawillygene.Smart.Inventory.Management.System.entity.Inventory;
import com.dawillygene.Smart.Inventory.Management.System.entity.StockMovement;
import com.dawillygene.Smart.Inventory.Management.System.service.InventoryService;
import com.dawillygene.Smart.Inventory.Management.System.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/inventory")
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private ProductService productService;

    /**
     * Inventory overview page
     */
    @GetMapping
    public String inventoryOverview(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "product.name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String search,
            Model model) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Inventory> inventoryPage;
        
        if (search != null && !search.trim().isEmpty()) {
            inventoryPage = inventoryService.searchInventoryByProductName(search, pageable);
        } else {
            inventoryPage = inventoryService.findAllInventory(pageable);
        }
        
        List<Inventory> lowStockItems = inventoryService.findLowStockItems();
        
        model.addAttribute("inventoryPage", inventoryPage);
        model.addAttribute("lowStockItems", lowStockItems);
        model.addAttribute("lowStockCount", lowStockItems.size());
        model.addAttribute("totalItems", inventoryPage.getTotalElements());
        model.addAttribute("currentPage", page);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("search", search);
        
        return "inventory/overview";
    }

    /**
     * Stock movements history page
     */
    @GetMapping("/movements")
    public String stockMovements(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            Model model) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<StockMovement> movementsPage = inventoryService.getRecentStockMovements(pageable);
        
        model.addAttribute("movementsPage", movementsPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        
        return "inventory/movements";
    }

    /**
     * Low stock alerts page
     */
    @GetMapping("/low-stock")
    public String lowStockAlerts(Model model) {
        List<Inventory> lowStockItems = inventoryService.findLowStockItems();
        
        model.addAttribute("lowStockItems", lowStockItems);
        model.addAttribute("lowStockCount", lowStockItems.size());
        
        return "inventory/low-stock";
    }

    /**
     * Show stock adjustment form
     */
    @GetMapping("/adjust/{id}")
    public String showAdjustmentForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Inventory inventory = inventoryService.findInventoryByProduct(id)
                .orElseThrow(() -> new RuntimeException("Inventory not found for product ID: " + id));
            
            model.addAttribute("inventory", inventory);
            return "inventory/adjust";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error loading adjustment form: " + e.getMessage());
            return "redirect:/inventory";
        }
    }

    /**
     * Process stock adjustment
     */
    @PostMapping("/adjust/{id}")
    public String adjustStock(@PathVariable Long id, 
                            @RequestParam Integer newQuantity,
                            @RequestParam(required = false) String reason,
                            RedirectAttributes redirectAttributes) {
        try {
            String adjustmentReason = reason != null && !reason.trim().isEmpty() ? 
                reason : "Manual stock adjustment";
            
            inventoryService.adjustStockTo(id, newQuantity, adjustmentReason);
            redirectAttributes.addFlashAttribute("success", "Stock adjusted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error adjusting stock: " + e.getMessage());
        }
        return "redirect:/inventory";
    }

    /**
     * Get inventory details for AJAX requests
     */
    @GetMapping("/details/{productId}")
    @ResponseBody
    public Inventory getInventoryDetails(@PathVariable Long productId) {
        return inventoryService.findInventoryByProduct(productId)
            .orElse(null);
    }

    /**
     * Update inventory location
     */
    @PostMapping("/update-location/{id}")
    public String updateLocation(@PathVariable Long id,
                               @RequestParam String location,
                               RedirectAttributes redirectAttributes) {
        try {
            inventoryService.updateInventoryDetails(id, location);
            redirectAttributes.addFlashAttribute("success", "Inventory location updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating location: " + e.getMessage());
        }
        return "redirect:/inventory";
    }
}
