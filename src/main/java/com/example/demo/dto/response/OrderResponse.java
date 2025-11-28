package com.example.demo.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
@Data
@Builder
public class OrderResponse {
    private String id;
    private String code;
    private String status;
    private BigDecimal totalPrice;
    private String note;
    private LocalDateTime createdAt;
    private List<OrderDetailResponse> details;

    @Data
    @Builder
    public static class OrderDetailResponse {
        private String id;
        private String ingredientName;
        private Integer quantity;
        private BigDecimal unit;
        private BigDecimal unitPrice;
        private BigDecimal totalPrice;
    }
}
