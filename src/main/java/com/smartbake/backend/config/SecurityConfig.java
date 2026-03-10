package com.smartbake.backend.config;

import com.smartbake.backend.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())

            .authorizeHttpRequests(auth -> auth
                // Public endpoints (no login needed)
                .requestMatchers(
                    "/api/v2/auth/**",   // login, register
                     "/api/products",
                     "/api/cart/debug", 
                    "/login",
                    "/register",
                    "/css/**",
                    "/images/**",
                    "/uploads/**",
                    "/"
                ).permitAll()

                // Admin-only REST endpoints
                .requestMatchers("/api/admin/**").hasRole("ADMIN")

                // Customer + Admin REST endpoints
                .requestMatchers("/api/**").authenticated()

                // Everything else (Thymeleaf pages) - authenticated
                .anyRequest().authenticated()
            )
            // After .userDetailsService(userDetailsService) add:
.exceptionHandling(ex -> ex
    .authenticationEntryPoint((request, response, authException) -> {
        // Return 401 JSON instead of redirecting to /login
        if (request.getRequestURI().startsWith("/api/")) {
            response.setStatus(401);
            response.setContentType("application/json");
            response.getWriter().write("{\"success\":false,\"message\":\"Not authenticated\"}");
        } else {
            response.sendRedirect("/login");
        }
    })
)

            .formLogin(form -> form
                .loginPage("/login")
                .permitAll()
                .defaultSuccessUrl("/dashboard", true)
            )

            .logout(logout -> logout
                .permitAll()
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
            )

            .userDetailsService(userDetailsService);
            

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // THIS IS WHAT WAS MISSING - needed by AuthApiController
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}