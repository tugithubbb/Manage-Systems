package com.example.demo.repository;

import com.example.demo.Entity.DeliveryOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryOrderRepository extends JpaRepository<DeliveryOrder,String> {
}
