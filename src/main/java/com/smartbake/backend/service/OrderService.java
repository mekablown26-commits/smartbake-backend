package com.smartbake.backend.service;

import com.smartbake.backend.entity.Order;
import com.smartbake.backend.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

   public List<Order> findByUserEmail(String email) {
    return orderRepository.findByUserEmail(email);
}

    // Add more methods later (findAll, findById, save, etc.)
}