package com.smartbake.backend.service;

import com.smartbake.backend.entity.Product;
import java.util.List;
import java.util.Optional;

public interface ProductService {

    List<Product> findAllActive();

    List<Product> findAll();

    Product save(Product product);

    void softDeleteById(Long id);

    Optional<Product> findById(Long id); // ✅ changed from Object to Optional<Product>
}