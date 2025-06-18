package com.dawillygene.Smart.Inventory.Management.System.controller;

import com.dawillygene.Smart.Inventory.Management.System.entity.Supplier;
import com.dawillygene.Smart.Inventory.Management.System.entity.Product;
import com.dawillygene.Smart.Inventory.Management.System.service.SupplierService;
import com.dawillygene.Smart.Inventory.Management.System.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

/**
 * Controller for supplier management
 */
@Controller
@RequestMapping("/suppliers")
public class SupplierController {

    @Autowired
    private SupplierService supplierService;
    
    @Autowired
    private ProductService productService;

    /**
     * Display paginated list of suppliers
     */
    @GetMapping
    public String listSuppliers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String search,
            Model model) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Supplier> supplierPage;
        
        if (search != null && !search.trim().isEmpty()) {
            supplierPage = supplierService.searchSuppliers(search, pageable);
        } else {
            supplierPage = supplierService.findAllSuppliers(pageable);
        }
        
        // Add product count for each supplier
        supplierPage.getContent().forEach(supplier -> {
            try {
                Long productCount = productService.countProductsBySupplier(supplier.getId());
                supplier.getProducts(); // This will set the product count via getProductCount()
            } catch (Exception e) {
                // Handle gracefully
            }
        });
        
        model.addAttribute("supplierPage", supplierPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("search", search);
        model.addAttribute("currentUri", "/suppliers");
        
        return "suppliers/list";
    }

    /**
     * Show form for adding new supplier
     */
    @GetMapping("/add")
    public String showAddSupplierForm(Model model) {
        model.addAttribute("supplier", new Supplier());
        model.addAttribute("isEdit", false);
        model.addAttribute("currentUri", "/suppliers/add");
        return "suppliers/form";
    }

    /**
     * Show form for editing existing supplier
     */
    @GetMapping("/edit/{id}")
    public String showEditSupplierForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Optional<Supplier> supplierOpt = supplierService.findById(id);
            if (supplierOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Supplier not found.");
                return "redirect:/suppliers";
            }
            
            model.addAttribute("supplier", supplierOpt.get());
            model.addAttribute("isEdit", true);
            model.addAttribute("currentUri", "/suppliers/edit/" + id);
            return "suppliers/form";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Supplier not found.");
            return "redirect:/suppliers";
        }
    }

    /**
     * Save supplier (both create and update)
     */
    @PostMapping("/save")
    public String saveSupplier(@Valid @ModelAttribute Supplier supplier, 
                              BindingResult result, 
                              Model model, 
                              RedirectAttributes redirectAttributes) {
        
        if (result.hasErrors()) {
            model.addAttribute("isEdit", supplier.getId() != null);
            return "suppliers/form";
        }

        try {
            Supplier savedSupplier;
            if (supplier.getId() != null) {
                // Update existing supplier
                savedSupplier = supplierService.updateSupplier(supplier.getId(), supplier);
            } else {
                // Create new supplier
                savedSupplier = supplierService.createSupplier(supplier);
            }
            
            String action = supplier.getId() == null ? "created" : "updated";
            redirectAttributes.addFlashAttribute("success", 
                "Supplier '" + savedSupplier.getName() + "' " + action + " successfully!");
            return "redirect:/suppliers";
        } catch (Exception e) {
            model.addAttribute("error", "Error saving supplier: " + e.getMessage());
            model.addAttribute("isEdit", supplier.getId() != null);
            return "suppliers/form";
        }
    }

    /**
     * View supplier details
     */
    @GetMapping("/view/{id}")
    public String viewSupplier(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Optional<Supplier> supplierOpt = supplierService.findById(id);
            if (supplierOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Supplier not found.");
                return "redirect:/suppliers";
            }
            
            Supplier supplier = supplierOpt.get();
            model.addAttribute("supplier", supplier);
            model.addAttribute("currentUri", "/suppliers/view/" + id);
            
            // Get products from this supplier
            try {
                List<Product> supplierProducts = productService.getProductsBySupplier(supplier.getId());
                model.addAttribute("supplierProducts", supplierProducts);
                model.addAttribute("productCount", supplierProducts.size());
            } catch (Exception e) {
                model.addAttribute("supplierProducts", List.of());
                model.addAttribute("productCount", 0);
            }
            
            return "suppliers/view";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Supplier not found.");
            return "redirect:/suppliers";
        }
    }

    /**
     * Show supplier transaction history
     */
    @GetMapping("/history/{id}")
    public String viewSupplierHistory(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Optional<Supplier> supplierOpt = supplierService.findById(id);
            if (supplierOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Supplier not found.");
                return "redirect:/suppliers";
            }
            
            Supplier supplier = supplierOpt.get();
            model.addAttribute("supplier", supplier);
            model.addAttribute("currentUri", "/suppliers/history/" + id);
            
            // Get purchase history - this would be implemented later with Order management
            // For now, just show empty list
            model.addAttribute("transactions", List.of());
            
            return "suppliers/history";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Supplier not found.");
            return "redirect:/suppliers";
        }
    }

    /**
     * Delete supplier
     */
    @PostMapping("/delete/{id}")
    public String deleteSupplier(@PathVariable Long id, 
                                RedirectAttributes redirectAttributes) {
        try {
            Optional<Supplier> supplierOpt = supplierService.findById(id);
            if (supplierOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Supplier not found.");
                return "redirect:/suppliers";
            }
            
            Supplier supplier = supplierOpt.get();
            
            // Check if supplier has products
            List<Product> products = productService.getProductsBySupplier(supplier.getId());
            if (!products.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", 
                    "Cannot delete supplier '" + supplier.getName() + "' because it has " + 
                    products.size() + " products assigned to it.");
                return "redirect:/suppliers";
            }
            
            supplierService.deleteSupplier(id);
            redirectAttributes.addFlashAttribute("success", 
                "Supplier '" + supplier.getName() + "' deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Error deleting supplier: " + e.getMessage());
        }
        
        return "redirect:/suppliers";
    }

    /**
     * Toggle supplier active status
     */
    @PostMapping("/toggle-status/{id}")
    public String toggleSupplierStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Optional<Supplier> supplierOpt = supplierService.findById(id);
            if (supplierOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Supplier not found.");
                return "redirect:/suppliers";
            }
            
            Supplier supplier = supplierOpt.get();
            supplier.setIsActive(!supplier.getIsActive());
            supplierService.updateSupplier(supplier.getId(), supplier);
            
            String status = supplier.getIsActive() ? "activated" : "deactivated";
            redirectAttributes.addFlashAttribute("success", 
                "Supplier '" + supplier.getName() + "' " + status + " successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Error updating supplier status: " + e.getMessage());
        }
        
        return "redirect:/suppliers";
    }

    /**
     * Get suppliers (AJAX endpoint for dropdowns)
     */
    @GetMapping("/api/active")
    @ResponseBody
    public List<Supplier> getActiveSuppliers() {
        return supplierService.findAllActiveSuppliers();
    }

    /**
     * Get supplier details (AJAX endpoint)
     */
    @GetMapping("/api/{id}")
    @ResponseBody
    public Supplier getSupplierById(@PathVariable Long id) {
        try {
            return supplierService.findById(id).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }
}
