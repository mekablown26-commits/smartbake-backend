package com.smartbake.backend.service;

import com.smartbake.backend.entity.User;
import com.smartbake.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public void registerUser(String fullName, String email, String phone, String password) {

        // 1️⃣ Check if email already exists
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already registered");
        }

        // 2️⃣ Encrypt password
        String encodedPassword = passwordEncoder.encode(password);

        // 3️⃣ Create new user
        User user = new User();
        user.setName(fullName);
        user.setEmail(email);
        user.setPhone(phone);
        user.setPassword(encodedPassword);
        user.setRole("CUSTOMER");
        user.setEnabled(true);

        // 4️⃣ Save to database
        userRepository.save(user);
    }
}