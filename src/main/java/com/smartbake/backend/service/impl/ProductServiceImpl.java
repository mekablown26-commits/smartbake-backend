package com.smartbake.backend.service.impl;

import com.smartbake.backend.entity.Product;
import com.smartbake.backend.repository.ProductRepository;
import com.smartbake.backend.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Override
    public List<Product> findAllActive() {
        return productRepository.findByDeletedFalse();
    }

    @Override
    public List<Product> findAll() {
        return productRepository.findAll();
    }

    @Override
    public Product save(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }
        return productRepository.save(product);
    }

    @Override
    public void softDeleteById(Long id) {
        if (id != null) {
            productRepository.findById(id).ifPresent(product -> {
                product.setDeleted(true);
                productRepository.save(product);
            });
        }
    }

    @Override
    public Object findById(Long id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findById'");
    }
}