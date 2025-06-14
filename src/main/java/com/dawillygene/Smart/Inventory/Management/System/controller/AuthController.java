package com.dawillygene.Smart.Inventory.Management.System.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller for authentication-related pages
 */
@Controller
public class AuthController {
    
    /**
     * Home page - redirect to dashboard if authenticated, otherwise to login
     */
    @GetMapping("/")
    public String home() {
        return "redirect:/dashboard";
    }
    
    /**
     * Login page
     */
    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                       @RequestParam(value = "logout", required = false) String logout,
                       Model model) {
        
        if (error != null) {
            model.addAttribute("error", "Invalid username or password!");
        }
        
        if (logout != null) {
            model.addAttribute("message", "You have been logged out successfully!");
        }
        
        return "auth/login";
    }
    
    /**
     * Access denied page
     */
    @GetMapping("/access-denied")
    public String accessDenied() {
        return "error/access-denied";
    }
}
