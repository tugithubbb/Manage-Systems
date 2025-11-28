package com.example.demo.services;

import com.example.demo.Entity.*;
import com.example.demo.dto.request.CreateOrderRequest;
import com.example.demo.dto.response.OrderResponse;
import com.example.demo.mapper.OrderMapper;
import com.example.demo.repository.IngredientRepository;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.RestaurantRepository;
import com.example.demo.repository.RestaurantUserRepository;
import com.example.demo.util.JsonLogger;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderService {
    RestaurantRepository restaurantRepository;
    RestaurantUserRepository restaurantUserRepository;
    IngredientRepository ingredientRepository;
    OrderRepository orderRepository;


    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request, String userId, String restaurantId) {
        RestaurantUser user = restaurantUserRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));

        // Create order
        Order order = new Order();
        order.setCode(request.getCode());
        order.setRestaurant(restaurant); // ⭐ SET restaurant
        order.setRestaurantUser(user);
        order.setStatus(OrderStatus.PENDING);
        order.setNote(request.getNote());

        BigDecimal totalPrice = BigDecimal.ZERO;

        // Create order details
        List<OrderDetail> details = new ArrayList<>();
        for (var item : request.getItems()) {
            Ingredient ingredient = ingredientRepository.findById(item.getIngredientId())
                    .orElseThrow(() -> new RuntimeException("Ingredient not found"));

            OrderDetail detail = new OrderDetail();
            detail.setOrder(order);
            detail.setCode(request.getCode());
            detail.setIngredient(ingredient);
            detail.setQuantity(item.getQuantity());
            detail.setUnitPriceAtOrder(ingredient.getUnitPrice());

            BigDecimal subtotal = ingredient.getUnitPrice()
                    .multiply(BigDecimal.valueOf(item.getQuantity()));
            detail.setTotalPrice(subtotal);

            details.add(detail);
            totalPrice = totalPrice.add(subtotal);
        }

        order.setTotalPrice(totalPrice);
        order.setOrderDetails(details);
        System.out.println("Order code = " + order.getCode());
        System.out.println("Restaurant ID = " + restaurant.getId());
        System.out.println("User ID = " + user.getId());
        orderRepository.save(order);
        return toOrderResponse(order);
    }

    public List<OrderResponse> getOrdersByRestaurant(String restaurantId) {
        long startTime = System.currentTimeMillis();
        try{
            List<Order> orders = orderRepository.findByRestaurantId(restaurantId);
            List<OrderResponse> response = orders.stream()
                    .map(this::toOrderResponse)
                    .collect(Collectors.toList());
            return response;
        }catch (Exception exception){
            throw exception;
        }
    }
    public OrderResponse getOrderDetail(String orderId, String restaurantId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // ⭐ KIỂM TRA: Order có thuộc restaurant này không?
        if (!order.getRestaurant().getId().equals(restaurantId)) {
            throw new RuntimeException("Access denied");
        }

        return toOrderResponse(order);
    }
    @Transactional
    public OrderResponse updateOrder(String orderId, CreateOrderRequest request, String restaurantId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // ⭐ KIỂM TRA: Order có thuộc restaurant này không?
        if (!order.getRestaurant().getId().equals(restaurantId)) {
            throw new RuntimeException("Access denied");
        }

        // Only allow update if status is PENDING
        if (!"PENDING".equals(order.getStatus())) {
            throw new RuntimeException("Cannot update order with status: " + order.getStatus());
        }
        order.setNote(request.getNote());
        order.setCode(request.getCode());
        orderRepository.save(order);
        return toOrderResponse(order);
    }
    @Transactional
    public void deleteOrder(String orderId, String restaurantId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // ⭐ KIỂM TRA: Order có thuộc restaurant này không?
        if (!order.getRestaurant().getId().equals(restaurantId)) {
            throw new RuntimeException("Access denied");
        }

        // Soft delete
        order.setDeletedAt(LocalDateTime.now());
        orderRepository.save(order);
    }
    public OrderResponse toOrderResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .code(order.getCode())
                .status(order.getStatus().name())
                .totalPrice(order.getTotalPrice())
                .note(order.getNote())
                .createdAt(order.getCreatedAt())
                .details(order.getOrderDetails().stream().map(d ->
                        OrderResponse.OrderDetailResponse.builder()
                                .id(d.getId())
                                .ingredientName(d.getIngredient().getName())
                                .quantity(d.getQuantity())
                                .unit(d.getIngredient().getUnitPrice())
                                .unitPrice(d.getUnitPriceAtOrder())
                                .totalPrice(d.getTotalPrice())
                                .build()
                ).collect(Collectors.toList()))
                .build();
    }

}
