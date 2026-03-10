// ═══════════════════════════════════════════════════════════
// FILE 3: controller/CartApiController.java — NEW FILE
// Create at: src/main/java/com/smartbake/backend/controller/
// ═══════════════════════════════════════════════════════════

package com.smartbake.backend.controller;

import com.smartbake.backend.dto.CartItem;
import com.smartbake.backend.entity.Order;
import com.smartbake.backend.entity.OrderItem;
import com.smartbake.backend.entity.Product;
import com.smartbake.backend.entity.User;
import com.smartbake.backend.repository.OrderRepository;
import com.smartbake.backend.repository.ProductRepository;
import com.smartbake.backend.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/cart")
@CrossOrigin(origins = "*", allowCredentials = "false")
public class CartApiController {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    private static final String CART_KEY = "cart";

    public CartApiController(ProductRepository productRepository,
                              UserRepository userRepository,
                              OrderRepository orderRepository) {
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
    }

    // ── GET /api/cart ─────────────────────────────────────
    @GetMapping
    public ResponseEntity<?> getCart(HttpSession session) {
        List<CartItem> cart = getCartFromSession(session);
        double total = cart.stream().mapToDouble(CartItem::getSubtotal).sum();

        List<Map<String, Object>> items = new ArrayList<>();
        for (CartItem item : cart) {
            Map<String, Object> productMap = new LinkedHashMap<>();
            productMap.put("id", item.getProduct().getId());
            productMap.put("name", item.getProduct().getName());
            productMap.put("price", item.getProduct().getPrice());
            productMap.put("imageUrl", item.getProduct().getImageUrl());

            Map<String, Object> itemMap = new LinkedHashMap<>();
            itemMap.put("product", productMap);
            itemMap.put("quantity", item.getQuantity());
            itemMap.put("subtotal", item.getSubtotal());
            items.add(itemMap);
        }

        return ResponseEntity.ok(Map.of(
                "items", items,
                "total", total,
                "count", cart.size()
        ));
    }

    // ── POST /api/cart/add/{productId} ────────────────────
    @PostMapping("/add/{productId}")
    public ResponseEntity<?> addToCart(@PathVariable Long productId,
                                        @RequestParam(defaultValue = "1") int quantity,
                                        HttpSession session) {
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Product not found"));
        }
        if (product.getStock() != null && product.getStock() < quantity) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false,
                            "message", "Only " + product.getStock() + " in stock"));
        }

        List<CartItem> cart = getCartFromSession(session);
        boolean found = false;
        for (CartItem item : cart) {
            if (item.getProduct().getId().equals(productId)) {
                item.setQuantity(item.getQuantity() + quantity);
                found = true;
                break;
            }
        }
        if (!found) {
            cart.add(new CartItem(product, quantity));
        }
        session.setAttribute(CART_KEY, cart);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", product.getName() + " added to cart 🛒",
                "cartCount", cart.size()
        ));
    }

    // ── POST /api/cart/remove/{productId} ─────────────────
    @PostMapping("/remove/{productId}")
    public ResponseEntity<?> removeFromCart(@PathVariable Long productId,
                                             HttpSession session) {
        List<CartItem> cart = getCartFromSession(session);
        cart.removeIf(item -> item.getProduct().getId().equals(productId));
        session.setAttribute(CART_KEY, cart);

        double total = cart.stream().mapToDouble(CartItem::getSubtotal).sum();
        return ResponseEntity.ok(Map.of(
                "success", true,
                "cartCount", cart.size(),
                "total", total
        ));
    }

    // ── POST /api/cart/clear ──────────────────────────────
    @PostMapping("/clear")
    public ResponseEntity<?> clearCart(HttpSession session) {
        session.removeAttribute(CART_KEY);
        return ResponseEntity.ok(Map.of("success", true));
    }

    // ── POST /api/cart/checkout ───────────────────────────
    @PostMapping("/checkout")
    public ResponseEntity<?> checkout(@RequestBody Map<String, String> body,
                                       Authentication authentication,
                                       HttpSession session) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401)
                    .body(Map.of("success", false, "message", "Please login first"));
        }

        List<CartItem> cart = getCartFromSession(session);
        if (cart.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Your cart is empty"));
        }

        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return ResponseEntity.status(404)
                    .body(Map.of("success", false, "message", "User not found"));
        }

        String customerName = body.get("customerName");
        String customerPhone = body.get("customerPhone");
        if (customerName == null || customerName.isBlank() ||
            customerPhone == null || customerPhone.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Name and phone are required"));
        }

        Order order = new Order();
        order.setUser(user);
        order.setStatus("PENDING");
        order.setCreatedAt(LocalDateTime.now());
        order.setCustomerName(customerName);
        order.setCustomerPhone(customerPhone);
        order.setDeliveryAddress(body.getOrDefault("deliveryAddress", ""));
        order.setNotes(body.getOrDefault("notes", ""));
        order.setPaymentMethod(body.getOrDefault("paymentMethod", "CASH"));
        order.setPaybillNumber("0716624266");

        List<OrderItem> orderItems = new ArrayList<>();
        double total = 0.0;

        for (CartItem cartItem : cart) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPriceAtPurchase(cartItem.getProduct().getPrice());
            orderItem.setSubtotal(cartItem.getQuantity() * cartItem.getProduct().getPrice());
            orderItems.add(orderItem);
            total += orderItem.getSubtotal();
        }

        order.setItems(orderItems);
        order.setTotalAmount(total);
        orderRepository.save(order);

        session.removeAttribute(CART_KEY);

        String year = String.valueOf(order.getCreatedAt().getYear()).substring(2);
        String orderNumber = String.format("SB-%s-%03d", year, order.getId());

        return ResponseEntity.ok(Map.of(
                "success", true,
                "orderId", order.getId(),
                "orderNumber", orderNumber,
                "total", order.getTotalAmount(),
                "status", order.getStatus(),
                "paymentMethod", order.getPaymentMethod(),
                "message", "Order placed successfully! 🎉"
        ));
    }

    @SuppressWarnings("unchecked")
    private List<CartItem> getCartFromSession(HttpSession session) {
        List<CartItem> cart = (List<CartItem>) session.getAttribute(CART_KEY);
        if (cart == null) {
            cart = new ArrayList<>();
            session.setAttribute(CART_KEY, cart);
        }
        return cart;
    }

    @GetMapping("/debug")
    public ResponseEntity<?> debugSession(HttpSession session,
                                           Authentication authentication) {
        return ResponseEntity.ok(Map.of(
                "sessionId", session.getId(),
                "isAuthenticated", authentication != null && authentication.isAuthenticated(),
                "username", authentication != null ? authentication.getName() : "anonymous",
                "cartSize", getCartFromSession(session).size()
        ));
    }
}