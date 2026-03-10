package com.smartbake.backend.controller;

import com.smartbake.backend.entity.User;
import com.smartbake.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ──────────────────────────────────────────────
    // SHOW LOGIN PAGE (already working)
    // ──────────────────────────────────────────────
    @GetMapping("/login")
    public String showLoginPage() {
        return "login";  // renders templates/login.html
    }

    // ──────────────────────────────────────────────
    // SHOW SIGN-UP PAGE ← this is the missing piece
    // ──────────────────────────────────────────────
    @GetMapping("/register")
    public String showRegisterPage() {
        return "register";  // renders templates/register.html
    }

    // ──────────────────────────────────────────────
    // HANDLE SIGN-UP FORM SUBMISSION
    // ──────────────────────────────────────────────
    @PostMapping("/register")
    public String registerUser(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String phone,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            Model model) {

        // Basic validation
        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Passwords do not match!");
            return "register";
        }

        if (userRepository.findByEmail(email).isPresent()) {
            model.addAttribute("error", "Email is already registered.");
            return "register";
        }

        // Create new customer account
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPhone(phone);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole("CUSTOMER");  // default role for new users

        userRepository.save(user);

        model.addAttribute("success", "Account created successfully! You can now sign in.");
        return "register";
    }
}