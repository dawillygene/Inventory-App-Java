package com.dawillygene.Smart.Inventory.Management.System.controller;

import com.dawillygene.Smart.Inventory.Management.System.entity.Category;
import com.dawillygene.Smart.Inventory.Management.System.service.CategoryService;
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
@RequestMapping("/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * Display paginated list of categories
     */
    @GetMapping
    public String listCategories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String search,
            Model model) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Category> categoryPage;
        
        if (search != null && !search.trim().isEmpty()) {
            categoryPage = categoryService.searchCategories(search, pageable);
        } else {
            categoryPage = categoryService.findAllCategories(pageable);
        }
        
        // Populate product count for each category
        categoryPage.getContent().forEach(category -> {
            try {
                Long productCount = categoryService.getProductCountByCategory(category.getId());
                category.setProductCount(productCount);
            } catch (Exception e) {
                category.setProductCount(0L);
            }
        });
        
        model.addAttribute("categoryPage", categoryPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("search", search);
        
        return "categories/list";
    }

    /**
     * Show form for adding new category
     */
    @GetMapping("/add")
    public String showAddCategoryForm(Model model) {
        model.addAttribute("category", new Category());
        model.addAttribute("isEdit", false);
        return "categories/form";
    }

    /**
     * Show form for editing existing category
     */
    @GetMapping("/edit/{id}")
    public String showEditCategoryForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Category category = categoryService.findCategoryById(id);
            model.addAttribute("category", category);
            model.addAttribute("isEdit", true);
            return "categories/form";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Category not found.");
            return "redirect:/categories";
        }
    }

    /**
     * Save category (both create and update)
     */
    @PostMapping("/save")
    public String saveCategory(@Valid @ModelAttribute Category category, 
                              BindingResult result, 
                              Model model, 
                              RedirectAttributes redirectAttributes) {
        
        System.out.println("DEBUG: Saving category: " + category.getName() + ", Active: " + category.getIsActive());
        
        if (result.hasErrors()) {
            System.out.println("DEBUG: Validation errors: " + result.getAllErrors());
            model.addAttribute("isEdit", category.getId() != null);
            return "categories/form";
        }

        // Check for duplicate name (excluding current category if editing)
        if (categoryService.existsByNameAndNotId(category.getName(), category.getId())) {
            System.out.println("DEBUG: Duplicate category name detected");
            model.addAttribute("error", "A category with this name already exists.");
            model.addAttribute("isEdit", category.getId() != null);
            return "categories/form";
        }
        
        try {
            Category savedCategory = categoryService.saveCategory(category);
            System.out.println("DEBUG: Category saved successfully with ID: " + savedCategory.getId());
            String action = category.getId() == null ? "created" : "updated";
            redirectAttributes.addFlashAttribute("success", 
                "Category '" + savedCategory.getName() + "' " + action + " successfully!");
            return "redirect:/categories";
        } catch (Exception e) {
            System.out.println("DEBUG: Error saving category: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Error saving category: " + e.getMessage());
            model.addAttribute("isEdit", category.getId() != null);
            return "categories/form";
        }
    }

    /**
     * View category details
     */
    @GetMapping("/view/{id}")
    public String viewCategory(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Category category = categoryService.findCategoryById(id);
            model.addAttribute("category", category);
            
            // Get product count for this category
            try {
                Long productCount = categoryService.getProductCountByCategory(category.getId());
                model.addAttribute("productCount", productCount);
            } catch (Exception e) {
                model.addAttribute("productCount", 0L);
            }
            
            return "categories/view";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Category not found.");
            return "redirect:/categories";
        }
    }

    /**
     * Delete category
     */
    @PostMapping("/delete/{id}")
    public String deleteCategory(@PathVariable Long id, 
                                RedirectAttributes redirectAttributes) {
        try {
            Category category = categoryService.findCategoryById(id);
            
            // Check if category has products
            if (category.getProducts() != null && !category.getProducts().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", 
                    "Cannot delete category '" + category.getName() + "' because it has " + 
                    category.getProducts().size() + " products assigned to it.");
                return "redirect:/categories";
            }
            
            categoryService.deleteCategory(id);
            redirectAttributes.addFlashAttribute("success", 
                "Category '" + category.getName() + "' deleted successfully!");
        } catch (Exception e) {
            System.out.println("DEBUG: Error deleting category: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", 
                "Error deleting category: " + e.getMessage());
        }
        
        return "redirect:/categories";
    }

    /**
     * Toggle category active status
     */
    @PostMapping("/toggle-status/{id}")
    public String toggleCategoryStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Category category = categoryService.findCategoryById(id);
            category.setIsActive(!category.isActive());
            categoryService.saveCategory(category);
            
            String status = category.isActive() ? "activated" : "deactivated";
            redirectAttributes.addFlashAttribute("success", 
                "Category '" + category.getName() + "' " + status + " successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Error updating category status: " + e.getMessage());
        }
        
        return "redirect:/categories";
    }

    /**
     * Get categories (AJAX endpoint for dropdowns)
     */
    @GetMapping("/api/active")
    @ResponseBody
    public List<Category> getActiveCategories() {
        return categoryService.findAllActiveCategories();
    }

    /**
     * Get category details (AJAX endpoint)
    @ResponseBody
    public Category getCategoryById(@PathVariable Long id) {
        try {
            return categoryService.findCategoryById(id);
        } catch (Exception e) {
            return null;
        }
    }
}
