package com.smartbake.backend.controller;

import com.smartbake.backend.entity.Order;
import com.smartbake.backend.entity.Product;
import com.smartbake.backend.repository.OrderRepository;
import com.smartbake.backend.repository.ProductRepository;
import com.smartbake.backend.repository.OrderItemRepository;
import com.smartbake.backend.service.CloudinaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private CloudinaryService cloudinaryService;

    // ───── Product Management ─────

    @GetMapping("/products")
    public String listProducts(Model model) {
        model.addAttribute("products", productRepository.findByDeletedFalse());
        return "admin/products";
    }

    @GetMapping("/products/new")
    public String showNewProductForm(Model model) {
        model.addAttribute("product", new Product());
        return "admin/product-form";
    }

    @PostMapping("/products")
    public String saveProduct(@ModelAttribute Product product,
                              @RequestParam("imageFile") MultipartFile imageFile,
                              Model model) throws IOException {

        if (!imageFile.isEmpty()) {
            String imageUrl = cloudinaryService.uploadImage(imageFile);
            product.setImageUrl(imageUrl);
        }

        Product saved = productRepository.save(product);
        if (saved != null) {
            model.addAttribute("product", saved);
        }
        return "admin/product-success";
    }

    @GetMapping("/products/edit/{id}")
    public String showEditForm(@PathVariable long id, Model model) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid product Id: " + id));
        model.addAttribute("product", product);
        return "admin/product-form";
    }

    @PostMapping("/products/edit/{id}")
    public String updateProduct(@PathVariable long id,
                                @ModelAttribute Product updatedProduct,
                                @RequestParam("imageFile") MultipartFile imageFile,
                                RedirectAttributes redirectAttributes) throws IOException {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        product.setName(updatedProduct.getName());
        product.setDescription(updatedProduct.getDescription());
        product.setPrice(updatedProduct.getPrice());
        product.setCategory(updatedProduct.getCategory());
        product.setStock(updatedProduct.getStock());

        if (!imageFile.isEmpty()) {
            // Delete old image from Cloudinary
            cloudinaryService.deleteImage(product.getImageUrl());
            // Upload new image to Cloudinary
            String imageUrl = cloudinaryService.uploadImage(imageFile);
            product.setImageUrl(imageUrl);
        }

        productRepository.save(product);
        redirectAttributes.addFlashAttribute("message", "Product updated successfully!");
        return "redirect:/admin/products";
    }

    @PostMapping("/products/delete/{id}")
    public String deleteProduct(@PathVariable long id, RedirectAttributes redirectAttributes) {
        try {
            Product product = productRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Product not found"));
            // Delete image from Cloudinary
            cloudinaryService.deleteImage(product.getImageUrl());
            product.setDeleted(true);
            productRepository.save(product);
            redirectAttributes.addFlashAttribute("message", "Product removed successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
        }
        return "redirect:/admin/products";
    }

    // ───── Orders ─────

    @GetMapping("/orders")
    public String listOrders(Model model) {
        List<Order> orders = orderRepository.findAll();
        model.addAttribute("orders", orders);
        return "admin/orders";
    }

    @GetMapping("/orders/{id}")
    public String orderDetails(@PathVariable long id, Model model) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid order Id: " + id));
        model.addAttribute("order", order);
        model.addAttribute("isAdmin", true);
        return "admin/order-details";
    }

    @PostMapping("/orders/{id}/update-delivery")
    public String updateDeliveryInfo(
            @PathVariable long id,
            @RequestParam String riderName,
            @RequestParam String riderPhone,
            @RequestParam String riderBikePlate,
            @RequestParam Double deliveryFee,
            RedirectAttributes redirectAttributes) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid order Id: " + id));
        order.setRiderName(riderName);
        order.setRiderPhone(riderPhone);
        order.setRiderBikePlate(riderBikePlate);
        order.setDeliveryFee(deliveryFee);
        orderRepository.save(order);
        redirectAttributes.addFlashAttribute("message", "Delivery info updated!");
        return "redirect:/admin/orders/" + id;
    }

    @PostMapping("/orders/{id}/update-status")
    public String updateOrderStatus(@PathVariable long id,
                                    @RequestParam("status") String newStatus,
                                    Model model) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid order Id: " + id));
        if (!List.of("PENDING", "PROCESSING", "READY", "DELIVERED", "CANCELLED").contains(newStatus)) {
            model.addAttribute("error", "Invalid status");
            model.addAttribute("order", order);
            return "admin/order-details";
        }
        order.setStatus(newStatus);
        orderRepository.save(order);
        return "redirect:/admin/orders/" + id;
    }

    @GetMapping("/contact-settings")
    public String showContactSettings(Model model) {
        return "admin/contact-settings";
    }

    @PostMapping("/contact/update")
    public String updateContact(@RequestParam String phone, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("message", "Contact number updated to " + phone);
        return "redirect:/admin/contact-settings";
    }
}
