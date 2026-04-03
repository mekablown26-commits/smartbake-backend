package com.smartbake.backend.controller;
 
import com.smartbake.backend.entity.Product;
import com.smartbake.backend.repository.ProductRepository;
import com.smartbake.backend.service.CloudinaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
 
import java.util.List;
import java.util.Map;
 
@RestController
@CrossOrigin(origins = "*")
public class ProductApiController {
 
    private final ProductRepository productRepository;
 
    @Autowired
    private CloudinaryService cloudinaryService;
 
    public ProductApiController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }
 
    // ─── Customer: GET /api/products ─────────────────────────
    // Returns only active (non-deleted) products for the shop
    @GetMapping("/api/products")
    public ResponseEntity<List<Product>> getProducts() {
        List<Product> products = productRepository.findAll()
                .stream()
                .filter(p -> !Boolean.TRUE.equals(p.isDeleted()))
                .toList();
        return ResponseEntity.ok(products);
    }
 
    // ─── Admin: GET /api/admin/products ──────────────────────
    // Returns ALL non-deleted products for admin management view
    @GetMapping("/api/admin/products")
    public ResponseEntity<List<Product>> getAdminProducts() {
        List<Product> products = productRepository.findAll()
                .stream()
                .filter(p -> !Boolean.TRUE.equals(p.isDeleted()))
                .toList();
        return ResponseEntity.ok(products);
    }
 
    // ─── Admin: DELETE /api/admin/products/delete/{id} ───────
    // Soft deletes a product (sets deleted = true)
    @PostMapping("/api/admin/products/delete/{id}")
    public ResponseEntity<Map<String, Object>> deleteProduct(@PathVariable long id) {
        try {
            Product product = productRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Product not found: " + id));
 
            // Delete image from Cloudinary if it exists
            if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
                try {
                    cloudinaryService.deleteImage(product.getImageUrl());
                } catch (Exception e) {
                    // Log but don't fail the delete if Cloudinary fails
                    System.err.println("Cloudinary delete failed: " + e.getMessage());
                }
            }
 
            // Soft delete
            product.setDeleted(true);
            productRepository.save(product);
 
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Product deleted successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Failed to delete: " + e.getMessage()
            ));
        }
    }
}