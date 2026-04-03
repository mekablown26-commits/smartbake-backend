package com.smartbake.backend.controller;
 
import com.smartbake.backend.entity.Product;
import com.smartbake.backend.service.ProductService;
import com.smartbake.backend.service.CloudinaryService;
import com.smartbake.backend.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
 
import java.util.List;
import java.util.Map;
import java.util.Optional;
 
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ProductRestController {
 
    @Autowired
    private ProductService productService;
 
    @Autowired
    private ProductRepository productRepository;
 
    @Autowired
    private CloudinaryService cloudinaryService;
 
    // ─── Customer: GET /api/products ─────────────────────────
    // Returns only active (non-deleted) products for the shop
    @GetMapping("/products")
    public ResponseEntity<List<Product>> getActiveProducts() {
        List<Product> products = productService.findAllActive();
        return ResponseEntity.ok(products);
    }
 
    // ─── Customer: GET /api/products/{id} ────────────────────
    @GetMapping("/products/{id}")
    public ResponseEntity<Object> getProductById(@PathVariable Long id) {
        Optional<Object> product = Optional.ofNullable(productService.findById(id));
        return product.map(p -> ResponseEntity.ok(p))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
 
    // ─── Admin: GET /api/admin/products ──────────────────────
    // Returns ALL non-deleted products for admin management view
 @GetMapping("/admin/products")
public ResponseEntity<List<Product>> getAdminProducts() {
    List<Product> products = productRepository.findByDeletedFalse();  // ✅ this
    return ResponseEntity.ok(products);
}
 
    // ─── Admin: POST /api/admin/products/delete/{id} ─────────
    // Soft deletes a product (sets deleted = true)
    @PostMapping("/admin/products/delete/{id}")
    public ResponseEntity<Map<String, Object>> deleteProduct(@PathVariable long id) {
        try {
            Product product = productRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Product not found: " + id));
 
            // Delete image from Cloudinary if it exists
            if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
                try {
                    cloudinaryService.deleteImage(product.getImageUrl());
                } catch (Exception e) {
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