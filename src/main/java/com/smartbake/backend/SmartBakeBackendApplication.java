package com.smartbake.backend;

import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.lang.NonNull;


@SpringBootApplication
public class SmartBakeBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartBakeBackendApplication.class, args);
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addViewControllers(@NonNull ViewControllerRegistry registry) {
                registry.addViewController("/").setViewName("index");
            }
        };
    }
}