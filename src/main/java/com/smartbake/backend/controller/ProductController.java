package com.smartbake.backend.controller;

import com.smartbake.backend.entity.Product;
import com.smartbake.backend.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class ProductController {

    @Autowired
    private ProductService productService;

    // Customer: browse treats
    @GetMapping("/products")
    public String showCustomerProducts(Model model) {
        List<Product> products = productService.findAllActive();
        model.addAttribute("products", products);
        return "products";
    }

    // Admin: add product form
    @GetMapping("/admin/products/add")
    public String showAddProductForm(Model model) {
        model.addAttribute("product", new Product());
        return "admin/product-form";
    }

}