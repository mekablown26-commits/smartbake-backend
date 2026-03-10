package com.smartbake.backend.controller;

import com.smartbake.backend.dto.AuthRequest;
import com.smartbake.backend.dto.RegisterRequest;
import com.smartbake.backend.entity.User;
import com.smartbake.backend.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/v2/auth")
@CrossOrigin(origins = "http://192.168.100.37:8080", allowCredentials = "true")
public class AuthApiController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public AuthApiController(UserRepository userRepository,
                              PasswordEncoder passwordEncoder,
                              AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request, HttpSession session) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );
            SecurityContextHolder.getContext().setAuthentication(auth);
            session.setAttribute("SPRING_SECURITY_CONTEXT",
                    SecurityContextHolder.getContext());

            User user = userRepository.findByEmail(request.getUsername())
                    .orElseThrow();

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "role", user.getRole(),
                    "email", user.getEmail(),
                    "fullName", user.getName() != null ? user.getName() : ""
            ));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401)
                    .body(Map.of("success", false, "message", "Invalid email or password"));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("success", false, "message", "Login error: " + e.getMessage()));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Email already registered"));
        }

        // ← Matches your entity fields exactly: name, email, password, phone, role, enabled, createdAt
        User user = new User();
        user.setName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("CUSTOMER");
        user.setEnabled(true);
        user.setCreatedAt(LocalDateTime.now());

        userRepository.save(user);  // ← save(), NOT saveAll()

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Account created successfully"
        ));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(Map.of("success", true));
    }
}