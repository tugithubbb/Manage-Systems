package com.example.demo.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {
    private String note;
    private String code;
    private List<OrderItemRequest> items;

    @Data
    public static class OrderItemRequest {
        private String ingredientId;
        private Integer quantity;
    }
}
