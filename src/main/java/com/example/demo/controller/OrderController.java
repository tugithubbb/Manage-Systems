package com.example.demo.controller;

import com.example.demo.dto.request.CreateOrderRequest;
import com.example.demo.dto.response.OrderResponse;
import com.example.demo.services.OrderService;
import com.example.demo.util.JsonLogger;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class OrderController {
    OrderService orderService;
    @PostMapping("/create")
    public ResponseEntity<OrderResponse> createOrder(
            @RequestBody CreateOrderRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getClaimAsString("user_id");
        String restaurantId = jwt.getClaimAsString("restaurant_id");
        OrderResponse order = orderService.createOrder(request, userId, restaurantId);
        return ResponseEntity.ok(order);
    }


        @GetMapping("/restaurant")
    public ResponseEntity<List<OrderResponse>> getOrders(@AuthenticationPrincipal Jwt jwt) {
        long startTime = System.currentTimeMillis();
        String restaurantId = jwt.getClaimAsString("restaurant_id");
            try{
                List<OrderResponse> orders = orderService.getOrdersByRestaurant(restaurantId);

                return ResponseEntity.ok(orders);
            }catch (Exception exception){
                throw exception;
            }
    }
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrderDetail(
            @PathVariable String orderId,
            @AuthenticationPrincipal Jwt jwt) {
        String restaurantId = jwt.getClaimAsString("restaurant_id");
        OrderResponse order = orderService.getOrderDetail(orderId, restaurantId);
        return ResponseEntity.ok(order);
    }
    @DeleteMapping("/{orderId}")
    public ResponseEntity<?> deleteOrder(
            @PathVariable String orderId,
            @AuthenticationPrincipal Jwt jwt) {
        String restaurantId = jwt.getClaimAsString("restaurant_id");

        orderService.deleteOrder(orderId, restaurantId);
        return ResponseEntity.ok().body(Map.of(
                "success", true,
                "message", "Đã xóa đơn hàng"
        ));
    }

    @PutMapping("/update/{orderId}")
    public ResponseEntity<OrderResponse> updateOrder(
            @PathVariable String orderId,
            @RequestBody CreateOrderRequest request,
           @AuthenticationPrincipal Jwt jwt) {
        String restaurantId = jwt.getClaimAsString("restaurant_id");
        OrderResponse order = orderService.updateOrder(orderId, request, restaurantId);
        return ResponseEntity.ok(order);
    }

}
