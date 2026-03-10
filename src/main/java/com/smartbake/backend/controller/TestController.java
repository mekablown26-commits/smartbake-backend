package com.smartbake.backend.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/api/hello")
    public String hello() {
        return "Hello from SmartBake API! Server is alive 🚀";
    }

    @GetMapping("/api/admin-only")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminOnly() {
        return "Welcome, Admin! You can manage everything here. 😎";
    }
}