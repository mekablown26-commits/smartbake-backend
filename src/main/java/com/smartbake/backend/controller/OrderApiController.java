// ═══════════════════════════════════════════════════════════
// FILE 4: controller/OrderApiController.java — NEW FILE
// Create at: src/main/java/com/smartbake/backend/controller/
// ═══════════════════════════════════════════════════════════

package com.smartbake.backend.controller;

import com.smartbake.backend.entity.Order;
import com.smartbake.backend.entity.OrderItem;
import com.smartbake.backend.repository.OrderRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@CrossOrigin(origins = "*", allowCredentials = "false")
public class OrderApiController {

    private final OrderRepository orderRepository;

    public OrderApiController(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    // ── GET /api/my-orders ────────────────────────────────
    @GetMapping("/api/my-orders")
    public ResponseEntity<?> getMyOrders(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401)
                    .body(Map.of("success", false, "message", "Not authenticated"));
        }
        List<Order> orders = orderRepository.findByUserEmail(authentication.getName());
        List<Map<String, Object>> result = new ArrayList<>();
        for (Order order : orders) result.add(buildOrderMap(order, false));
        return ResponseEntity.ok(result);
    }

    // ── POST /api/orders/{id}/cancel ──────────────────────
    @PostMapping("/api/orders/{id}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable Long id,
                                          Authentication authentication) {
        Order order = orderRepository.findById(id).orElse(null);
        if (order == null) {
            return ResponseEntity.status(404)
                    .body(Map.of("success", false, "message", "Order not found"));
        }
        if (!order.getUser().getEmail().equals(authentication.getName())) {
            return ResponseEntity.status(403)
                    .body(Map.of("success", false, "message", "Not your order"));
        }
        if (!List.of("PENDING", "PROCESSING").contains(order.getStatus())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false,
                            "message", "Cannot cancel — order is " + order.getStatus()));
        }
        order.setStatus("CANCELLED");
        orderRepository.save(order);
        return ResponseEntity.ok(Map.of("success", true, "message", "Order cancelled"));
    }

    // ── GET /api/admin/orders ─────────────────────────────
    @GetMapping("/api/admin/orders")
    public ResponseEntity<?> getAllOrders() {
        List<Order> orders = orderRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Order order : orders) result.add(buildOrderMap(order, true));
        return ResponseEntity.ok(result);
    }

    // ── POST /api/admin/orders/{id}/update-status ─────────
    @PostMapping("/api/admin/orders/{id}/update-status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id,
                                           @RequestBody Map<String, String> body) {
        String newStatus = body.get("status");
        if (!List.of("PENDING", "PROCESSING", "READY", "DELIVERED", "CANCELLED")
                .contains(newStatus)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Invalid status"));
        }
        Order order = orderRepository.findById(id).orElse(null);
        if (order == null) {
            return ResponseEntity.status(404)
                    .body(Map.of("success", false, "message", "Order not found"));
        }
        order.setStatus(newStatus);
        orderRepository.save(order);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "orderId", order.getId(),
                "newStatus", order.getStatus()
        ));
    }

    // ── Helper ────────────────────────────────────────────
    private Map<String, Object> buildOrderMap(Order order, boolean isAdmin) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", order.getId());
        map.put("status", order.getStatus());
        map.put("totalAmount", order.getTotalAmount());
        map.put("paymentMethod", order.getPaymentMethod());
        map.put("createdAt", order.getCreatedAt() != null ? order.getCreatedAt().toString() : null);
        map.put("deliveryAddress", order.getDeliveryAddress());
        map.put("notes", order.getNotes());
        map.put("customerName", order.getCustomerName());
        map.put("customerPhone", order.getCustomerPhone());

        if (isAdmin && order.getUser() != null) {
            map.put("userEmail", order.getUser().getEmail());
        }

        List<Map<String, Object>> items = new ArrayList<>();
        for (OrderItem item : order.getItems()) {
            Map<String, Object> itemMap = new LinkedHashMap<>();
            itemMap.put("quantity", item.getQuantity());
            itemMap.put("priceAtPurchase", item.getPriceAtPurchase());
            itemMap.put("subtotal", item.getSubtotal());
            if (item.getProduct() != null) {
                Map<String, Object> productMap = new LinkedHashMap<>();
                productMap.put("id", item.getProduct().getId());
                productMap.put("name", item.getProduct().getName());
                productMap.put("imageUrl", item.getProduct().getImageUrl());
                itemMap.put("product", productMap);
            }
            items.add(itemMap);
        }
        map.put("items", items);
        return map;
    }
}






















































