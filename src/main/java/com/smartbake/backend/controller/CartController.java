package com.smartbake.backend.controller;

import com.smartbake.backend.dto.CartItem;
import com.smartbake.backend.entity.Order;
import com.smartbake.backend.entity.OrderItem;
import com.smartbake.backend.entity.Product;
import com.smartbake.backend.entity.User;
import com.smartbake.backend.repository.OrderRepository;
import com.smartbake.backend.repository.OrderItemRepository;
import com.smartbake.backend.repository.ProductRepository;
import com.smartbake.backend.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.access.prepost.PreAuthorize;
import java.util.Optional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    private static final String CART_SESSION_KEY = "cart";

    // ─────────────────────────────────────────────
    //  Add to Cart – cleaned up, better UX
    // ─────────────────────────────────────────────

    @PostMapping("/add/{productId}")
    public String addToCart(@PathVariable Long productId,
                            @RequestParam(defaultValue = "1") int quantity,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {

        if (productId == null) {
            throw new RuntimeException("Product ID cannot be null");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Optional but recommended: stock check
        if (product.getStock() != null && product.getStock() < quantity) {
            redirectAttributes.addFlashAttribute("error", "Not enough stock! Only " + product.getStock() + " left.");
            return "redirect:/products";
        }

        List<CartItem> cart = getCart(session);
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

        session.setAttribute(CART_SESSION_KEY, cart);

        // Flash message (shows after redirect)
        redirectAttributes.addFlashAttribute("message",
                product.getName() + " × " + quantity + " added to cart! 🛒");

        // Stay on products page – best user flow
        return "redirect:/products";
    }

    // ─────────────────────────────────────────────
    //  View Cart (perfect as is)
    // ─────────────────────────────────────────────

    @GetMapping
    public String viewCart(HttpSession session, Model model) {
        List<CartItem> cart = getCart(session);
        double total = cart.stream().mapToDouble(CartItem::getSubtotal).sum();

        model.addAttribute("cartItems", cart);
        model.addAttribute("total", total);
        return "cart";
    }

    // ─────────────────────────────────────────────
    //  Remove & Clear (perfect)
    // ─────────────────────────────────────────────

    @PostMapping("/remove/{productId}")
    public String removeFromCart(@PathVariable Long productId, HttpSession session) {
        List<CartItem> cart = getCart(session);
        cart.removeIf(item -> item.getProduct().getId().equals(productId));
        session.setAttribute(CART_SESSION_KEY, cart);
        return "redirect:/cart";
    }

    @PostMapping("/clear")
    public String clearCart(HttpSession session) {
        session.removeAttribute(CART_SESSION_KEY);
        return "redirect:/cart";
    }

    @SuppressWarnings("unchecked")
    private List<CartItem> getCart(HttpSession session) {
        List<CartItem> cart = (List<CartItem>) session.getAttribute(CART_SESSION_KEY);
        if (cart == null) {
            cart = new ArrayList<>();
            session.setAttribute(CART_SESSION_KEY, cart);
        }
        return cart;
    }

    // ─────────────────────────────────────────────
    //  Checkout GET (perfect)
    // ─────────────────────────────────────────────

    @GetMapping("/checkout")
    public String showCheckout(HttpSession session, Model model, Authentication authentication) {
        List<CartItem> cart = getCart(session);

        if (cart.isEmpty()) {
            model.addAttribute("message", "Your cart is empty!");
            return "redirect:/cart";
        }

        double total = cart.stream().mapToDouble(CartItem::getSubtotal).sum();

        model.addAttribute("cartItems", cart);
        model.addAttribute("total", total);
        return "checkout";
    }

    // ─────────────────────────────────────────────
    //  Checkout POST – cleaned up & safe
    // ─────────────────────────────────────────────

    @PostMapping("/checkout")
    public String checkout(
            @RequestParam("customerName") String customerName,
            @RequestParam("customerPhone") String customerPhone,
            @RequestParam(value = "deliveryAddress", required = false) String deliveryAddress,
            @RequestParam(value = "notes", required = false) String notes,
            @RequestParam("paymentMethod") String paymentMethod,
            Authentication authentication,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (authentication == null || !authentication.isAuthenticated()) {
            redirectAttributes.addFlashAttribute("error", "Please login to checkout");
            return "redirect:/login";
        }

        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<CartItem> cart = getCart(session);

        if (cart.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Your cart is empty!");
            return "redirect:/cart";
        }

        Order order = new Order();
        order.setUser(user);
        order.setStatus("PENDING");
        order.setCreatedAt(LocalDateTime.now());

        order.setCustomerName(customerName);
        order.setCustomerPhone(customerPhone);
        order.setDeliveryAddress(deliveryAddress);
        order.setNotes(notes);
        order.setPaymentMethod(paymentMethod);
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

        // Nice short order number (SB-YY-NNN)
        String year = String.valueOf(order.getCreatedAt().getYear()).substring(2);
        String shortId = String.format("SB-%s-%03d", year, order.getId());

        // Flash attributes for success page
        redirectAttributes.addFlashAttribute("orderNumber", shortId);
        redirectAttributes.addFlashAttribute("orderId", order.getId());
        redirectAttributes.addFlashAttribute("total", order.getTotalAmount());
        redirectAttributes.addFlashAttribute("message", "Order placed successfully! Order #" + shortId);
        redirectAttributes.addFlashAttribute("order", order);  // full object

        session.removeAttribute(CART_SESSION_KEY);

        return "redirect:/cart/order-success";
    }

    @GetMapping("/order-success")
    public String showOrderSuccess() {
        return "order-success";
    }
}