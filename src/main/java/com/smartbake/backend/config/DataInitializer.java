package com.smartbake.backend.config;

import com.smartbake.backend.entity.User;
import com.smartbake.backend.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostConstruct
    public void init() {
        String adminEmail = "smartbakei.a.n@gmail.com";

        if (userRepository.findByEmail(adminEmail).isEmpty()) {
            User admin = new User();
            admin.setName("Admin");
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode("smartbakery101*"));
            admin.setRole("ADMIN");
            // admin.setPhone("..."); // optional

            userRepository.save(admin);
            System.out.println("===== ADMIN USER SEEDED SUCCESSFULLY =====");
        } else {
            System.out.println("Admin user already exists.");
        }
    }
}