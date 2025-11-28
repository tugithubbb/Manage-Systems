package com.example.demo.mapper;

import com.example.demo.Entity.Order;
import com.example.demo.dto.response.OrderResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    OrderResponse toOrderResponse(Order order);
}
