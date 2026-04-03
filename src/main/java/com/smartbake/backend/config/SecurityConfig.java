package com.smartbake.backend.config;
 
import com.smartbake.backend.service.CustomUserDetailsService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
 
import java.io.IOException;
import java.util.Collection;
 
@Configuration
@EnableWebSecurity
public class SecurityConfig {
 
    private final CustomUserDetailsService userDetailsService;
 
    public SecurityConfig(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }
 
    // ── Role-based success handler ────────────────────────────────────────────
    // After login, ADMIN → /dashboard, CUSTOMER → /dashboard
    // Both land on /dashboard but the dashboard.html shows different UI per role.
    // This prevents Spring Security from "remembering" a previous URL (like
    // /admin/orders) and redirecting a customer there after login.
    @Bean
    public AuthenticationSuccessHandler roleBasedSuccessHandler() {
        return new AuthenticationSuccessHandler() {
            @Override
            public void onAuthenticationSuccess(HttpServletRequest request,
                                                HttpServletResponse response,
                                                Authentication authentication) throws IOException {
 
                Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
 
                String redirectUrl = "/dashboard"; // default for everyone
 
                for (GrantedAuthority authority : authorities) {
                    if (authority.getAuthority().equals("ROLE_ADMIN")) {
                        redirectUrl = "/dashboard";
                        break;
                    }
                    if (authority.getAuthority().equals("ROLE_CUSTOMER")) {
                        redirectUrl = "/dashboard";
                        break;
                    }
                }
 
                // Clear the saved request so Spring doesn't redirect to a stale URL
                // (this is the key fix — stops it from going to /admin/orders)
                request.getSession().removeAttribute("SPRING_SECURITY_SAVED_REQUEST");
 
                response.sendRedirect(redirectUrl);
            }
        };
    }
 
    // ── Security filter chain ─────────────────────────────────────────────────
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
 
            .authorizeHttpRequests(auth -> auth
                // Public endpoints (no login needed)
                .requestMatchers(
                    "/api/v2/auth/**",
                    "/api/products",
                    "/api/cart/debug",
                    "/ping",
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
 
                // Everything else (Thymeleaf pages) — authenticated
                .anyRequest().authenticated()
            )
 
            // Return 401 JSON for API calls, redirect to /login for pages
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
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
                .successHandler(roleBasedSuccessHandler()) // ← replaces defaultSuccessUrl
                .permitAll()
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
 
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}