package com.smartbake.backend.repository;

import com.smartbake.backend.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserEmail(String email);

    // Add any other queries you need
}