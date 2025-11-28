package com.example.demo.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RestaurantUserResponse {

    private String id;
    private String username;
    private RoleDTO role;
    private RestaurantDTO restaurant;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RoleDTO {
        private String id;
        private String name;
        private String code;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RestaurantDTO {
        private String id;
        private String name;
        private String address;
    }
}
