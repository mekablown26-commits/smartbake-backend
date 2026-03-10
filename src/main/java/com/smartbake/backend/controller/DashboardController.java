package com.smartbake.backend.controller;

import com.smartbake.backend.dto.CartItem;           // ← this import was missing
import com.smartbake.backend.entity.Product;
import com.smartbake.backend.repository.ProductRepository;
import jakarta.servlet.http.HttpSession;           // ← needed for session
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class DashboardController {

    @Autowired
    private ProductRepository productRepository;

    @GetMapping("/dashboard")
    public String dashboard(Model model, 
                            Authentication authentication,
                            HttpSession session) {   // ← added HttpSession here

        String username = authentication.getName();
        String role = authentication.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");

        model.addAttribute("username", username);
        model.addAttribute("role", role);

        // Load products for both roles
        List<Product> products = productRepository.findAll();
        model.addAttribute("products", products);

        // Cart count logic (this is what we need for the badge)
        @SuppressWarnings("unchecked")
        List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");
        int cartCount = (cart != null) ? cart.size() : 0;
        model.addAttribute("cartCount", cartCount);

        if ("ADMIN".equals(role)) {
            model.addAttribute("welcomeMessage", "Welcome to Admin Dashboard! Manage orders, products, users.");
        } else {
            model.addAttribute("welcomeMessage", "Welcome to Customer Dashboard! Browse products and place orders.");
        }

        return "dashboard";
    }
}