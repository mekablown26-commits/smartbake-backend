package com.smartbake.backend.controller;

import com.smartbake.backend.entity.Product;
import com.smartbake.backend.repository.ProductRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/products")
@CrossOrigin(origins = "*")
public class ProductApiController {

    private final ProductRepository productRepository;

    public ProductApiController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // GET /api/products — returns ALL products as JSON (Flutter uses this)
    @GetMapping("/products")
    public ResponseEntity<List<Product>> getProducts() {
        List<Product> products = productRepository.findAll();
        return ResponseEntity.ok(products);
    }
}