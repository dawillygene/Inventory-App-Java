package com.dawillygene.Smart.Inventory.Management.System.controller;

import com.dawillygene.Smart.Inventory.Management.System.entity.Order;
import com.dawillygene.Smart.Inventory.Management.System.entity.OrderItem;
import com.dawillygene.Smart.Inventory.Management.System.entity.Product;
import com.dawillygene.Smart.Inventory.Management.System.entity.Supplier;
import com.dawillygene.Smart.Inventory.Management.System.entity.User;
import com.dawillygene.Smart.Inventory.Management.System.service.OrderService;
import com.dawillygene.Smart.Inventory.Management.System.service.ProductService;
import com.dawillygene.Smart.Inventory.Management.System.service.SupplierService;
import com.dawillygene.Smart.Inventory.Management.System.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Controller for order management
 */
@Controller
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;
    
    @Autowired
    private ProductService productService;
    
    @Autowired
    private SupplierService supplierService;
    
    @Autowired
    private UserService userService;

    /**
     * Display paginated list of orders
     */
    @GetMapping
    public String listOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "orderDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String orderType,
            @RequestParam(required = false) String orderStatus,
            Model model) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Order> orderPage = orderService.findAllOrders(pageable);
        
        model.addAttribute("orderPage", orderPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("search", search);
        model.addAttribute("selectedOrderType", orderType);
        model.addAttribute("selectedOrderStatus", orderStatus);
        model.addAttribute("currentUri", "/orders");
        
        // Add enum values for filters
        model.addAttribute("orderTypes", Order.OrderType.values());
        model.addAttribute("orderStatuses", Order.OrderStatus.values());
        
        return "orders/list";
    }

    /**
     * Show form for creating new purchase order
     */
    @GetMapping("/purchase/add")
    public String showAddPurchaseOrderForm(Model model) {
        Order order = new Order();
        order.setOrderType(Order.OrderType.PURCHASE);
        
        model.addAttribute("order", order);
        model.addAttribute("orderItems", new ArrayList<OrderItem>());
        model.addAttribute("products", productService.getAllActiveProducts());
        model.addAttribute("suppliers", supplierService.findAllActiveSuppliers());
        model.addAttribute("isEdit", false);
        model.addAttribute("orderType", "PURCHASE");
        model.addAttribute("currentUri", "/orders/purchase/add");
        
        return "orders/form";
    }

    /**
     * Show form for creating new sales order
     */
    @GetMapping("/sales/add")
    public String showAddSalesOrderForm(Model model) {
        Order order = new Order();
        order.setOrderType(Order.OrderType.SALE);
        
        model.addAttribute("order", order);
        model.addAttribute("orderItems", new ArrayList<OrderItem>());
        model.addAttribute("products", productService.getAllActiveProducts());
        model.addAttribute("isEdit", false);
        model.addAttribute("orderType", "SALE");
        model.addAttribute("currentUri", "/orders/sales/add");
        
        return "orders/form";
    }

    /**
     * Show form for editing existing order
     */
    @GetMapping("/edit/{id}")
    public String showEditOrderForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Optional<Order> orderOpt = orderService.findById(id);
            if (orderOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Order not found.");
                return "redirect:/orders";
            }
            
            Order order = orderOpt.get();
            
            // Check if order can be modified
            if (!order.canBeModified()) {
                redirectAttributes.addFlashAttribute("error", 
                    "Order cannot be modified in " + order.getOrderStatus() + " status.");
                return "redirect:/orders";
            }
            
            List<OrderItem> orderItems = orderService.getOrderItems(order.getId());
            
            model.addAttribute("order", order);
            model.addAttribute("orderItems", orderItems);
            model.addAttribute("products", productService.getAllActiveProducts());
            model.addAttribute("suppliers", supplierService.findAllActiveSuppliers());
            model.addAttribute("isEdit", true);
            model.addAttribute("orderType", order.getOrderType().name());
            model.addAttribute("currentUri", "/orders/edit/" + id);
            
            return "orders/form";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Order not found.");
            return "redirect:/orders";
        }
    }

    /**
     * Save order (both create and update)
     */
    @PostMapping("/save")
    public String saveOrder(@Valid @ModelAttribute Order order, 
                           BindingResult result, 
                           @RequestParam(value = "orderItems", required = false) String orderItemsJson,
                           Model model, 
                           RedirectAttributes redirectAttributes,
                           Authentication authentication) {
        
        if (result.hasErrors()) {
            model.addAttribute("products", productService.getAllActiveProducts());
            model.addAttribute("suppliers", supplierService.findAllActiveSuppliers());
            model.addAttribute("isEdit", order.getId() != null);
            model.addAttribute("orderType", order.getOrderType().name());
            return "orders/form";
        }

        try {
            // Get current user
            User currentUser = userService.getUserByUsername(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Current user not found"));
            order.setUser(currentUser);
            
            // For this simplified implementation, we'll create orders without items first
            // In a full implementation, you'd parse the orderItemsJson and create OrderItem objects
            List<OrderItem> orderItems = new ArrayList<>();
            
            Order savedOrder;
            if (order.getOrderType() == Order.OrderType.PURCHASE) {
                savedOrder = orderService.createPurchaseOrder(order, orderItems);
            } else {
                savedOrder = orderService.createSalesOrder(order, orderItems);
            }
            
            String orderTypeName = order.getOrderType() == Order.OrderType.PURCHASE ? "Purchase" : "Sales";
            redirectAttributes.addFlashAttribute("success", 
                orderTypeName + " order '" + savedOrder.getOrderNumber() + "' created successfully!");
            return "redirect:/orders/view/" + savedOrder.getId();
        } catch (Exception e) {
            model.addAttribute("error", "Error saving order: " + e.getMessage());
            model.addAttribute("products", productService.getAllActiveProducts());
            model.addAttribute("suppliers", supplierService.findAllActiveSuppliers());
            model.addAttribute("isEdit", order.getId() != null);
            model.addAttribute("orderType", order.getOrderType().name());
            return "orders/form";
        }
    }

    /**
     * View order details
     */
    @GetMapping("/view/{id}")
    public String viewOrder(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Optional<Order> orderOpt = orderService.findById(id);
            if (orderOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Order not found.");
                return "redirect:/orders";
            }
            
            Order order = orderOpt.get();
            List<OrderItem> orderItems = orderService.getOrderItems(order.getId());
            
            model.addAttribute("order", order);
            model.addAttribute("orderItems", orderItems);
            model.addAttribute("currentUri", "/orders/view/" + id);
            
            return "orders/view";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Order not found.");
            return "redirect:/orders";
        }
    }

    /**
     * Update order status
     */
    @PostMapping("/update-status/{id}")
    public String updateOrderStatus(@PathVariable Long id, 
                                   @RequestParam Order.OrderStatus newStatus,
                                   RedirectAttributes redirectAttributes) {
        try {
            Order updatedOrder = orderService.updateOrderStatus(id, newStatus);
            redirectAttributes.addFlashAttribute("success", 
                "Order status updated to " + newStatus.getDescription() + " successfully!");
            return "redirect:/orders/view/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Error updating order status: " + e.getMessage());
            return "redirect:/orders/view/" + id;
        }
    }

    /**
     * Update payment status
     */
    @PostMapping("/update-payment/{id}")
    public String updatePaymentStatus(@PathVariable Long id, 
                                     @RequestParam Order.PaymentStatus newPaymentStatus,
                                     RedirectAttributes redirectAttributes) {
        try {
            Order updatedOrder = orderService.updatePaymentStatus(id, newPaymentStatus);
            redirectAttributes.addFlashAttribute("success", 
                "Payment status updated to " + newPaymentStatus.getDescription() + " successfully!");
            return "redirect:/orders/view/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Error updating payment status: " + e.getMessage());
            return "redirect:/orders/view/" + id;
        }
    }

    /**
     * Cancel order
     */
    @PostMapping("/cancel/{id}")
    public String cancelOrder(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Optional<Order> orderOpt = orderService.findById(id);
            if (orderOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Order not found.");
                return "redirect:/orders";
            }
            
            Order order = orderOpt.get();
            if (!order.canBeCancelled()) {
                redirectAttributes.addFlashAttribute("error", 
                    "Order cannot be cancelled in " + order.getOrderStatus() + " status.");
                return "redirect:/orders/view/" + id;
            }
            
            orderService.updateOrderStatus(id, Order.OrderStatus.CANCELLED);
            redirectAttributes.addFlashAttribute("success", 
                "Order '" + order.getOrderNumber() + "' cancelled successfully!");
            return "redirect:/orders/view/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Error cancelling order: " + e.getMessage());
            return "redirect:/orders/view/" + id;
        }
    }

    /**
     * Delete order
     */
    @PostMapping("/delete/{id}")
    public String deleteOrder(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Optional<Order> orderOpt = orderService.findById(id);
            if (orderOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Order not found.");
                return "redirect:/orders";
            }
            
            Order order = orderOpt.get();
            orderService.deleteOrder(id);
            redirectAttributes.addFlashAttribute("success", 
                "Order '" + order.getOrderNumber() + "' deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Error deleting order: " + e.getMessage());
        }
        
        return "redirect:/orders";
    }

    /**
     * Show invoice for order
     */
    @GetMapping("/invoice/{id}")
    public String showInvoice(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Optional<Order> orderOpt = orderService.findById(id);
            if (orderOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Order not found.");
                return "redirect:/orders";
            }
            
            Order order = orderOpt.get();
            List<OrderItem> orderItems = orderService.getOrderItems(order.getId());
            
            model.addAttribute("order", order);
            model.addAttribute("orderItems", orderItems);
            model.addAttribute("currentUri", "/orders/invoice/" + id);
            
            return "orders/invoice";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Order not found.");
            return "redirect:/orders";
        }
    }

    /**
     * Get products (AJAX endpoint)
     */
    @GetMapping("/api/products")
    @ResponseBody
    public List<Product> getProducts() {
        return productService.getAllActiveProducts();
    }

    /**
     * Get product details (AJAX endpoint)
     */
    @GetMapping("/api/products/{id}")
    @ResponseBody
    public Product getProduct(@PathVariable Long id) {
        try {
            return productService.getProductById(id);
        } catch (Exception e) {
            return null;
        }
    }
}
