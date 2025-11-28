package com.example.demo.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRestaurantUserRequest {
    @Size(min = 3, max = 100, message = "Username must be between 3 and 100 characters")
    private String username;
    private String roleId;
    private String restaurantId;
}
