package com.smartbake.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthRestController {

    @GetMapping("/me")
    public ResponseEntity<?> me() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            String role = auth.getAuthorities().stream()
                .findFirst().map(a -> a.getAuthority().replace("ROLE_",""))
                .orElse("CUSTOMER");
            return ResponseEntity.ok(Map.of("role", role, "email", auth.getName()));
        }
        return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
    }

    @Autowired
    private com.smartbake.backend.service.UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body,
                                    HttpServletRequest request,
                                    HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            String role = auth.getAuthorities().stream()
                .findFirst().map(a -> a.getAuthority().replace("ROLE_",""))
                .orElse("CUSTOMER");
            return ResponseEntity.ok(Map.of("role", role, "email", auth.getName()));
        }
        return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        try {
            String fullName = body.get("fullName");
            String email = body.get("email");
            String phone = body.get("phone");
            String password = body.get("password");
            
            userService.registerUser(fullName, email, phone, password);
            
            return ResponseEntity.ok(Map.of("message", "Account created successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
