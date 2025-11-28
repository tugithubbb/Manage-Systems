package com.example.demo.repository;

import com.example.demo.Entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order,String> {
    List<Order> findByRestaurantId(String restaurantId);
}
