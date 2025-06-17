package com.dawillygene.Smart.Inventory.Management.System.controller;

import com.dawillygene.Smart.Inventory.Management.System.entity.Product;
import com.dawillygene.Smart.Inventory.Management.System.entity.Order;
import com.dawillygene.Smart.Inventory.Management.System.entity.Supplier;
import com.dawillygene.Smart.Inventory.Management.System.entity.Inventory;
import com.dawillygene.Smart.Inventory.Management.System.service.ProductService;
import com.dawillygene.Smart.Inventory.Management.System.service.OrderService;
import com.dawillygene.Smart.Inventory.Management.System.service.SupplierService;
import com.dawillygene.Smart.Inventory.Management.System.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for reports and analytics
 */
@Controller
@RequestMapping("/reports")
public class ReportController {

    @Autowired
    private ProductService productService;
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private SupplierService supplierService;
    
    @Autowired
    private InventoryService inventoryService;

    /**
     * Reports dashboard
     */
    @GetMapping
    public String reportsDashboard(Model model) {
        try {
            // Basic statistics
            long totalProducts = productService.getAllActiveProducts().size();
            long totalSuppliers = supplierService.findAllActiveSuppliers().size();
            long totalOrders = orderService.getTotalOrderCount();
            
            // Calculate total inventory value
            List<Inventory> allInventory = inventoryService.getAllInventory();
            BigDecimal totalInventoryValue = allInventory.stream()
                .map(inv -> inv.getProduct().getPrice().multiply(BigDecimal.valueOf(inv.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            // Low stock items count
            List<Inventory> lowStockItems = inventoryService.findLowStockItems();
            
            // Recent orders count (last 30 days)
            LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
            long recentOrdersCount = orderService.getOrdersCountSince(thirtyDaysAgo);
            
            // Add to model
            model.addAttribute("totalProducts", totalProducts);
            model.addAttribute("totalSuppliers", totalSuppliers);
            model.addAttribute("totalOrders", totalOrders);
            model.addAttribute("totalInventoryValue", totalInventoryValue);
            model.addAttribute("lowStockCount", lowStockItems.size());
            model.addAttribute("recentOrdersCount", recentOrdersCount);
            model.addAttribute("currentUri", "/reports");
            
        } catch (Exception e) {
            // Handle gracefully with default values
            model.addAttribute("totalProducts", 0L);
            model.addAttribute("totalSuppliers", 0L);
            model.addAttribute("totalOrders", 0L);
            model.addAttribute("totalInventoryValue", BigDecimal.ZERO);
            model.addAttribute("lowStockCount", 0L);
            model.addAttribute("recentOrdersCount", 0L);
            model.addAttribute("error", "Error loading dashboard data: " + e.getMessage());
        }
        
        return "reports/dashboard";
    }

    /**
     * Show report generation form
     */
    @GetMapping("/generate")
    public String showReportForm(Model model) {
        model.addAttribute("reportTypes", getAvailableReportTypes());
        model.addAttribute("suppliers", supplierService.findAllActiveSuppliers());
        model.addAttribute("currentUri", "/reports/generate");
        return "reports/form";
    }

    /**
     * Generate and display report
     */
    @PostMapping("/generate")
    public String generateReport(
            @RequestParam String reportType,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) Long supplierId,
            @RequestParam(required = false) String format,
            Model model) {
        
        try {
            // Parse dates
            LocalDate start = startDate != null && !startDate.isEmpty() ? 
                LocalDate.parse(startDate) : LocalDate.now().minusMonths(1);
            LocalDate end = endDate != null && !endDate.isEmpty() ? 
                LocalDate.parse(endDate) : LocalDate.now();
            
            // Generate report based on type
            Map<String, Object> reportData = generateReportData(reportType, start, end, supplierId);
            
            model.addAttribute("reportType", reportType);
            model.addAttribute("startDate", start);
            model.addAttribute("endDate", end);
            model.addAttribute("reportData", reportData);
            model.addAttribute("currentUri", "/reports/view");
            
            // Return appropriate view based on format
            if ("pdf".equals(format)) {
                return "reports/pdf"; // Will be implemented later
            } else if ("excel".equals(format)) {
                return "reports/excel"; // Will be implemented later
            } else {
                return "reports/view";
            }
            
        } catch (Exception e) {
            model.addAttribute("error", "Error generating report: " + e.getMessage());
            model.addAttribute("reportTypes", getAvailableReportTypes());
            model.addAttribute("suppliers", supplierService.findAllActiveSuppliers());
            return "reports/form";
        }
    }

    /**
     * Export report as CSV
     */
    @GetMapping("/export/{reportType}")
    public ResponseEntity<String> exportReport(
            @PathVariable String reportType,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) Long supplierId) {
        
        try {
            LocalDate start = startDate != null && !startDate.isEmpty() ? 
                LocalDate.parse(startDate) : LocalDate.now().minusMonths(1);
            LocalDate end = endDate != null && !endDate.isEmpty() ? 
                LocalDate.parse(endDate) : LocalDate.now();
            
            String csvContent = generateCsvReport(reportType, start, end, supplierId);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("text/csv"));
            headers.setContentDispositionFormData("attachment", reportType + "_report.csv");
            
            return ResponseEntity.ok()
                .headers(headers)
                .body(csvContent);
                
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body("Error generating CSV report: " + e.getMessage());
        }
    }

    /**
     * Get analytics data for charts (AJAX endpoint)
     */
    @GetMapping("/api/analytics/{type}")
    @ResponseBody
    public Map<String, Object> getAnalyticsData(@PathVariable String type) {
        Map<String, Object> data = new HashMap<>();
        
        try {
            switch (type) {
                case "inventory-levels":
                    data = getInventoryLevelsData();
                    break;
                case "orders-trend":
                    data = getOrdersTrendData();
                    break;
                case "top-products":
                    data = getTopProductsData();
                    break;
                case "supplier-performance":
                    data = getSupplierPerformanceData();
                    break;
                default:
                    data.put("error", "Unknown analytics type");
            }
        } catch (Exception e) {
            data.put("error", e.getMessage());
        }
        
        return data;
    }

    // Private helper methods
    
    private List<Map<String, String>> getAvailableReportTypes() {
        List<Map<String, String>> types = new ArrayList<>();
        
        Map<String, String> inventory = new HashMap<>();
        inventory.put("id", "inventory");
        inventory.put("name", "Inventory Report");
        inventory.put("description", "Current stock levels and inventory valuation");
        types.add(inventory);
        
        Map<String, String> sales = new HashMap<>();
        sales.put("id", "sales");
        sales.put("name", "Sales Report");
        sales.put("description", "Sales orders and revenue analysis");
        types.add(sales);
        
        Map<String, String> purchases = new HashMap<>();
        purchases.put("id", "purchases");
        purchases.put("name", "Purchase Report");
        purchases.put("description", "Purchase orders and supplier analysis");
        types.add(purchases);
        
        Map<String, String> lowStock = new HashMap<>();
        lowStock.put("id", "low-stock");
        lowStock.put("name", "Low Stock Report");
        lowStock.put("description", "Products with low inventory levels");
        types.add(lowStock);
        
        Map<String, String> supplier = new HashMap<>();
        supplier.put("id", "supplier");
        supplier.put("name", "Supplier Report");
        supplier.put("description", "Supplier performance and order history");
        types.add(supplier);
        
        return types;
    }
    
    private Map<String, Object> generateReportData(String reportType, LocalDate startDate, LocalDate endDate, Long supplierId) {
        Map<String, Object> data = new HashMap<>();
        
        switch (reportType) {
            case "inventory":
                data = generateInventoryReport();
                break;
            case "sales":
                data = generateSalesReport(startDate, endDate);
                break;
            case "purchases":
                data = generatePurchaseReport(startDate, endDate, supplierId);
                break;
            case "low-stock":
                data = generateLowStockReport();
                break;
            case "supplier":
                data = generateSupplierReport(supplierId);
                break;
            default:
                data.put("error", "Unknown report type");
        }
        
        return data;
    }
    
    private Map<String, Object> generateInventoryReport() {
        Map<String, Object> data = new HashMap<>();
        
        List<Inventory> inventory = inventoryService.getAllInventory();
        
        // Calculate totals
        BigDecimal totalValue = inventory.stream()
            .map(inv -> inv.getProduct().getPrice().multiply(BigDecimal.valueOf(inv.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        int totalItems = inventory.stream()
            .mapToInt(Inventory::getQuantity)
            .sum();
        
        data.put("items", inventory);
        data.put("totalValue", totalValue);
        data.put("totalItems", totalItems);
        data.put("totalProducts", inventory.size());
        
        return data;
    }
    
    private Map<String, Object> generateSalesReport(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> data = new HashMap<>();
        
        List<Order> salesOrders = orderService.findSalesOrdersByDateRange(
            startDate.atStartOfDay(), endDate.atTime(23, 59, 59));
        
        BigDecimal totalRevenue = salesOrders.stream()
            .map(Order::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        data.put("orders", salesOrders);
        data.put("totalRevenue", totalRevenue);
        data.put("totalOrders", salesOrders.size());
        data.put("averageOrderValue", salesOrders.isEmpty() ? BigDecimal.ZERO : 
            totalRevenue.divide(BigDecimal.valueOf(salesOrders.size()), 2, java.math.RoundingMode.HALF_UP));
        
        return data;
    }
    
    private Map<String, Object> generatePurchaseReport(LocalDate startDate, LocalDate endDate, Long supplierId) {
        Map<String, Object> data = new HashMap<>();
        
        List<Order> purchaseOrders = orderService.findPurchaseOrdersByDateRange(
            startDate.atStartOfDay(), endDate.atTime(23, 59, 59), supplierId);
        
        BigDecimal totalPurchaseValue = purchaseOrders.stream()
            .map(Order::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        data.put("orders", purchaseOrders);
        data.put("totalPurchaseValue", totalPurchaseValue);
        data.put("totalOrders", purchaseOrders.size());
        
        if (supplierId != null) {
            Supplier supplier = supplierService.findById(supplierId).orElse(null);
            data.put("supplier", supplier);
        }
        
        return data;
    }
    
    private Map<String, Object> generateLowStockReport() {
        Map<String, Object> data = new HashMap<>();
        
        List<Inventory> lowStockItems = inventoryService.findLowStockItems();
        
        BigDecimal lostSalesValue = lowStockItems.stream()
            .map(inv -> inv.getProduct().getPrice().multiply(BigDecimal.valueOf(10))) // Estimated lost sales
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        data.put("items", lowStockItems);
        data.put("totalItems", lowStockItems.size());
        data.put("estimatedLostSales", lostSalesValue);
        
        return data;
    }
    
    private Map<String, Object> generateSupplierReport(Long supplierId) {
        Map<String, Object> data = new HashMap<>();
        
        if (supplierId != null) {
            Supplier supplier = supplierService.findById(supplierId).orElse(null);
            data.put("supplier", supplier);
            
            // Get orders for this supplier
            List<Order> supplierOrders = orderService.findOrdersBySupplier(supplierId);
            data.put("orders", supplierOrders);
            
            // Get products from this supplier
            List<Product> supplierProducts = productService.getProductsBySupplier(supplierId);
            data.put("products", supplierProducts);
        } else {
            // All suppliers report
            List<Supplier> suppliers = supplierService.findAllActiveSuppliers();
            data.put("suppliers", suppliers);
        }
        
        return data;
    }
    
    private String generateCsvReport(String reportType, LocalDate startDate, LocalDate endDate, Long supplierId) {
        StringBuilder csv = new StringBuilder();
        
        switch (reportType) {
            case "inventory":
                csv = generateInventoryCsv();
                break;
            case "sales":
                csv = generateSalesCsv(startDate, endDate);
                break;
            case "purchases":
                csv = generatePurchasesCsv(startDate, endDate, supplierId);
                break;
            case "low-stock":
                csv = generateLowStockCsv();
                break;
            default:
                csv.append("Error: Unknown report type");
        }
        
        return csv.toString();
    }
    
    private StringBuilder generateInventoryCsv() {
        StringBuilder csv = new StringBuilder();
        csv.append("Product Name,SKU,Category,Supplier,Quantity,Unit Price,Total Value\n");
        
        List<Inventory> inventory = inventoryService.getAllInventory();
        for (Inventory inv : inventory) {
            Product product = inv.getProduct();
            csv.append(String.format("%s,%s,%s,%s,%d,%.2f,%.2f\n",
                product.getName(),
                product.getSku(),
                product.getCategory() != null ? product.getCategory().getName() : "",
                product.getSupplier() != null ? product.getSupplier().getName() : "",
                inv.getQuantity(),
                product.getPrice(),
                product.getPrice().multiply(BigDecimal.valueOf(inv.getQuantity()))
            ));
        }
        
        return csv;
    }
    
    private StringBuilder generateSalesCsv(LocalDate startDate, LocalDate endDate) {
        StringBuilder csv = new StringBuilder();
        csv.append("Order Number,Date,Customer,Total Amount,Status,Payment Status\n");
        
        List<Order> salesOrders = orderService.findSalesOrdersByDateRange(
            startDate.atStartOfDay(), endDate.atTime(23, 59, 59));
        
        for (Order order : salesOrders) {
            csv.append(String.format("%s,%s,%s,%.2f,%s,%s\n",
                order.getOrderNumber(),
                order.getOrderDate(),
                order.getCustomerName() != null ? order.getCustomerName() : "",
                order.getTotalAmount(),
                order.getOrderStatus().getDescription(),
                order.getPaymentStatus().getDescription()
            ));
        }
        
        return csv;
    }
    
    private StringBuilder generatePurchasesCsv(LocalDate startDate, LocalDate endDate, Long supplierId) {
        StringBuilder csv = new StringBuilder();
        csv.append("Order Number,Date,Supplier,Total Amount,Status,Payment Status\n");
        
        List<Order> purchaseOrders = orderService.findPurchaseOrdersByDateRange(
            startDate.atStartOfDay(), endDate.atTime(23, 59, 59), supplierId);
        
        for (Order order : purchaseOrders) {
            csv.append(String.format("%s,%s,%s,%.2f,%s,%s\n",
                order.getOrderNumber(),
                order.getOrderDate(),
                order.getSupplier() != null ? order.getSupplier().getName() : "",
                order.getTotalAmount(),
                order.getOrderStatus().getDescription(),
                order.getPaymentStatus().getDescription()
            ));
        }
        
        return csv;
    }
    
    private StringBuilder generateLowStockCsv() {
        StringBuilder csv = new StringBuilder();
        csv.append("Product Name,SKU,Current Quantity,Minimum Level,Recommended Order\n");
        
        List<Inventory> lowStockItems = inventoryService.findLowStockItems();
        for (Inventory inv : lowStockItems) {
            Product product = inv.getProduct();
            csv.append(String.format("%s,%s,%d,%d,%d\n",
                product.getName(),
                product.getSku(),
                inv.getQuantity(),
                inv.getProduct().getMinimumStockLevel(),
                Math.max(inv.getProduct().getMinimumStockLevel() * 2 - inv.getQuantity(), 0)
            ));
        }
        
        return csv;
    }
    
    // Analytics data methods for charts
    
    private Map<String, Object> getInventoryLevelsData() {
        Map<String, Object> data = new HashMap<>();
        List<Inventory> inventory = inventoryService.getAllInventory();
        
        List<String> labels = new ArrayList<>();
        List<Integer> quantities = new ArrayList<>();
        
        inventory.stream().limit(10).forEach(inv -> {
            labels.add(inv.getProduct().getName());
            quantities.add(inv.getQuantity());
        });
        
        data.put("labels", labels);
        data.put("data", quantities);
        
        return data;
    }
    
    private Map<String, Object> getOrdersTrendData() {
        Map<String, Object> data = new HashMap<>();
        
        // Get last 12 months data
        List<String> labels = new ArrayList<>();
        List<Long> orderCounts = new ArrayList<>();
        
        for (int i = 11; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusMonths(i);
            labels.add(date.getMonth().name().substring(0, 3) + " " + date.getYear());
            
            long count = orderService.getOrdersCountForMonth(date.getYear(), date.getMonthValue());
            orderCounts.add(count);
        }
        
        data.put("labels", labels);
        data.put("data", orderCounts);
        
        return data;
    }
    
    private Map<String, Object> getTopProductsData() {
        Map<String, Object> data = new HashMap<>();
        
        // This would need to be implemented in OrderService to get actual sales data
        // For now, just return sample data based on current inventory
        List<Inventory> inventory = inventoryService.getAllInventory();
        
        List<String> labels = new ArrayList<>();
        List<Integer> quantities = new ArrayList<>();
        
        inventory.stream()
            .sorted((a, b) -> Integer.compare(b.getQuantity(), a.getQuantity()))
            .limit(5)
            .forEach(inv -> {
                labels.add(inv.getProduct().getName());
                quantities.add(inv.getQuantity());
            });
        
        data.put("labels", labels);
        data.put("data", quantities);
        
        return data;
    }
    
    private Map<String, Object> getSupplierPerformanceData() {
        Map<String, Object> data = new HashMap<>();
        List<Supplier> suppliers = supplierService.findAllActiveSuppliers();
        
        List<String> labels = new ArrayList<>();
        List<Long> orderCounts = new ArrayList<>();
        
        suppliers.stream().limit(5).forEach(supplier -> {
            labels.add(supplier.getName());
            long count = orderService.getOrdersCountBySupplier(supplier.getId());
            orderCounts.add(count);
        });
        
        data.put("labels", labels);
        data.put("data", orderCounts);
        
        return data;
    }
}
