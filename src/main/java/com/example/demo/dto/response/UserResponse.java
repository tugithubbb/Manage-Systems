package com.example.demo.dto.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserResponse {
    private String id;
    private String username;
    private String email;
    private String fullname;
    private String phone;
    private String address;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    RoleAdminUser role;
    Restaurant restaurant;
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RoleAdminUser {
        private String id;
        private String name;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Restaurant {
        private String id;
        private String name;
        private String address;
    }


}

