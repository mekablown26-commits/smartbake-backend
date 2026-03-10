package com.smartbake.backend.repository;

import com.smartbake.backend.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import java.time.LocalDateTime;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByDeletedFalse();
}