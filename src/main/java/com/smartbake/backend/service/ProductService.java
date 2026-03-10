package com.smartbake.backend.service;

import com.smartbake.backend.entity.Product;

import java.util.List;

public interface ProductService {

    List<Product> findAllActive();

    List<Product> findAll();

    Product save(Product product);

    void softDeleteById(Long id);

    Object findById(Long id);
}