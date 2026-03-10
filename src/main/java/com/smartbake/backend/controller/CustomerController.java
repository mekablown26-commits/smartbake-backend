package com.smartbake.backend.controller;

import com.smartbake.backend.entity.Order;
import com.smartbake.backend.entity.User;
import com.smartbake.backend.repository.OrderRepository;
import com.smartbake.backend.repository.UserRepository;
import com.smartbake.backend.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.access.prepost.PreAuthorize;
import java.time.LocalDateTime;
import java.util.Optional;

import java.util.List;

@Controller
public class CustomerController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderService orderService;

    // Customer: see their own orders (main list page)
    @GetMapping("/my-orders")
    public String showMyOrders(Model model, Authentication authentication) {
        String email = authentication.getName(); // usually the email in Spring Security
        List<Order> orders = orderService.findByUserEmail(email);
        model.addAttribute("orders", orders);
        return "my-orders";
    }

    // Customer: view details of one of their orders (read-only)
    @GetMapping("/my-orders/{id}")
    public String myOrderDetails(@PathVariable Long id, Model model, Authentication authentication) {
        String email = authentication.getName();

        if (id == null) {
            model.addAttribute("error", "Invalid order ID!");
            return "redirect:/my-orders";
        }

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // Security: only allow if this order belongs to the logged-in user
        if (!order.getUser().getEmail().equals(email)) {
            model.addAttribute("error", "This is not your order!");
            return "redirect:/my-orders";
        }

        model.addAttribute("order", order);
        model.addAttribute("isAdmin", false); // hide admin controls

        return "admin/order-details";  // Reuse the same template (conditional)
    }

    // Customer: confirm received (only if READY)
    @PostMapping("/my-orders/{id}/confirm-received")
    public String confirmReceived(@PathVariable Long id, Authentication authentication, RedirectAttributes redirectAttributes) {
        String email = authentication.getName();

        if (id == null) {
            redirectAttributes.addFlashAttribute("error", "Invalid order ID!");
            return "redirect:/my-orders";
        }

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // Security check
        if (!order.getUser().getEmail().equals(email)) {
            redirectAttributes.addFlashAttribute("error", "This is not your order!");
            return "redirect:/my-orders";
        }

        // Only allow if READY
        if (!"READY".equals(order.getStatus())) {
            redirectAttributes.addFlashAttribute("error", "Order is not ready for confirmation yet.");
            return "redirect:/my-orders/" + id;
        }

        // Update to DELIVERED
        order.setStatus("DELIVERED");
        orderRepository.save(order);

        redirectAttributes.addFlashAttribute("message", "Order marked as received! Thank you!");
        return "redirect:/my-orders/" + id;
    }

    // In OrderController.java (or wherever your customer endpoints are)
@PostMapping("/orders/{orderId}/cancel")
@PreAuthorize("hasRole('CUSTOMER')")
public String cancelOrder(@PathVariable Long orderId,
                          Authentication authentication,
                          RedirectAttributes redirectAttributes) {

    String email = authentication.getName();
    User user = userRepository.findByEmail(email).orElseThrow();

    if (orderId == null) {
        redirectAttributes.addFlashAttribute("error", "Invalid order ID.");
        return "redirect:/my-orders";
    }

    Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new IllegalArgumentException("Order not found"));

    // Security: only owner can cancel
    if (!order.getUser().getId().equals(user.getId())) {
        redirectAttributes.addFlashAttribute("error", "You can only cancel your own orders.");
        return "redirect:/my-orders";
    }

    // Only allow cancel for early statuses
    if (!List.of("PENDING", "PROCESSING").contains(order.getStatus())) {
        redirectAttributes.addFlashAttribute("error", "This order cannot be cancelled anymore.");
        return "redirect:/my-orders";
    }

    order.setStatus("CANCELLED");
    orderRepository.save(order);

    redirectAttributes.addFlashAttribute("message", "Order cancelled successfully.");
    return "redirect:/my-orders";
}
}