package com.dawillygene.Smart.Inventory.Management.System.controller;

import com.dawillygene.Smart.Inventory.Management.System.entity.Product;
import com.dawillygene.Smart.Inventory.Management.System.entity.Category;
import com.dawillygene.Smart.Inventory.Management.System.entity.Supplier;
import com.dawillygene.Smart.Inventory.Management.System.service.ProductService;
import com.dawillygene.Smart.Inventory.Management.System.service.CategoryService;
import com.dawillygene.Smart.Inventory.Management.System.service.SupplierService;
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

@Controller
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductService productService;
    
    @Autowired
    private CategoryService categoryService;
    
    @Autowired
    private SupplierService supplierService;

    /**
     * Display paginated list of products
     */
    @GetMapping
    public String listProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long supplierId,
            Model model) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Product> productPage;
        
        if (search != null && !search.trim().isEmpty()) {
            productPage = productService.searchProducts(search, pageable);
        } else if (categoryId != null) {
            productPage = productService.findProductsByCategory(categoryId, pageable);
        } else if (supplierId != null) {
            productPage = productService.findProductsBySupplier(supplierId, pageable);
        } else {
            productPage = productService.findAllProducts(pageable);
        }
        
        // Get filter options for dropdowns
        List<Category> categories = categoryService.findAllActiveCategories();
        List<Supplier> suppliers = supplierService.findAllActiveSuppliers();
        
        model.addAttribute("productPage", productPage);
        model.addAttribute("categories", categories);
        model.addAttribute("suppliers", suppliers);
        model.addAttribute("currentPage", page);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("search", search);
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("selectedSupplierId", supplierId);
        
        return "products/list";
    }

    /**
     * Show form for adding new product
     */
    @GetMapping("/add")
    public String showAddProductForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryService.findAllActiveCategories());
        model.addAttribute("suppliers", supplierService.findAllActiveSuppliers());
        return "products/form";
    }

    /**
     * Show form for editing existing product
     */
    @GetMapping("/edit/{id}")
    public String showEditProductForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Product> productOpt = productService.findProductById(id);
        if (productOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Product not found.");
            return "redirect:/products";
        }
        
        model.addAttribute("product", productOpt.get());
        model.addAttribute("categories", categoryService.findAllActiveCategories());
        model.addAttribute("suppliers", supplierService.findAllActiveSuppliers());
        return "products/form";
    }

    /**
     * Save product (both create and update)
     */
    @PostMapping("/save")
    public String saveProduct(@Valid @ModelAttribute Product product, 
                            BindingResult result, 
                            Model model, 
                            RedirectAttributes redirectAttributes) {
        
        if (result.hasErrors()) {
            model.addAttribute("categories", categoryService.findAllActiveCategories());
            model.addAttribute("suppliers", supplierService.findAllActiveSuppliers());
            return "products/form";
        }
        
        try {
            Product savedProduct = productService.saveProduct(product);
            redirectAttributes.addFlashAttribute("success", 
                "Product '" + savedProduct.getName() + "' saved successfully!");
            return "redirect:/products";
        } catch (Exception e) {
            model.addAttribute("error", "Error saving product: " + e.getMessage());
            model.addAttribute("categories", categoryService.findAllActiveCategories());
            model.addAttribute("suppliers", supplierService.findAllActiveSuppliers());
            return "products/form";
        }
    }

    /**
     * View product details
     */
    @GetMapping("/view/{id}")
    public String viewProduct(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Product> productOpt = productService.findProductById(id);
        if (productOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Product not found.");
            return "redirect:/products";
        }
        
        Product product = productOpt.get();
        model.addAttribute("product", product);
        
        // Get inventory information for this product
        try {
            // This would need to be implemented in InventoryService
            // Integer currentStock = inventoryService.getCurrentStock(product.getId());
            // model.addAttribute("currentStock", currentStock);
        } catch (Exception e) {
            // Handle gracefully
        }
        
        return "products/view";
    }

    /**
     * Delete product
     */
    @PostMapping("/delete/{id}")
    public String deleteProduct(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Optional<Product> productOpt = productService.findProductById(id);
            if (productOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Product not found.");
                return "redirect:/products";
            }
            
            Product product = productOpt.get();
            productService.deleteProduct(id);
            redirectAttributes.addFlashAttribute("success", 
                "Product '" + product.getName() + "' deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Cannot delete product. It may be referenced by other records.");
        }
        
        return "redirect:/products";
    }

    /**
     * Toggle product active status
     */
    @PostMapping("/toggle-status/{id}")
    public String toggleProductStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Optional<Product> productOpt = productService.findProductById(id);
            if (productOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Product not found.");
                return "redirect:/products";
            }
            
            Product product = productOpt.get();
            product.setIsActive(!product.isActive());
            productService.saveProduct(product);
            
            String status = product.isActive() ? "activated" : "deactivated";
            redirectAttributes.addFlashAttribute("success", 
                "Product '" + product.getName() + "' " + status + " successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Error updating product status: " + e.getMessage());
        }
        
        return "redirect:/products";
    }

    /**
     * Get products by category (AJAX endpoint)
     */
    @GetMapping("/by-category/{categoryId}")
    @ResponseBody
    public List<Product> getProductsByCategory(@PathVariable Long categoryId) {
        return productService.findActiveProductsByCategory(categoryId);
    }

    /**
     * Get products by supplier (AJAX endpoint)
     */
    @GetMapping("/by-supplier/{supplierId}")
    @ResponseBody
    public List<Product> getProductsBySupplier(@PathVariable Long supplierId) {
        return productService.findActiveProductsBySupplier(supplierId);
    }
}
