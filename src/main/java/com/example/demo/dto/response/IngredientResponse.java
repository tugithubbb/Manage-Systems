package com.example.demo.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class IngredientResponse {
    private String id;
    private String code;
    private String name;
    private int stock;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
