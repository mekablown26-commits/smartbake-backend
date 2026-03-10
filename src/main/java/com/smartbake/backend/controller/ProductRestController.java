package com.smartbake.backend.controller;

import com.smartbake.backend.entity.Product;
import com.smartbake.backend.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")  // All mobile API calls start with /api/...
public class ProductRestController {

    @Autowired
    private ProductService productService;

    // Mobile app endpoint: returns JSON
    @GetMapping("/products")
    public ResponseEntity<List<Product>> getActiveProducts() {
        List<Product> products = productService.findAllActive();  // only non-deleted
        return ResponseEntity.ok(products);
    }

    // Optional: single product detail (for future product detail page in app)
    @GetMapping("/products/{id}")
    public ResponseEntity<Object> getProductById(@PathVariable Long id) {
        Optional<Object> product = Optional.ofNullable(productService.findById(id));
        return product.map(p -> ResponseEntity.ok(p))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}