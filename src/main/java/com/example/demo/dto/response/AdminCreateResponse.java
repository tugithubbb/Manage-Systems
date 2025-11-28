package com.example.demo.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
public class AdminCreateResponse {
    private String id;
    private String username;
    private String email;
    private String fullname;
    private String phone;
    private String address;
    private LocalDateTime createdAt;
    AdminRestaurant adminRestaurant;
    @Getter
    @Setter
    public static class AdminRestaurant {
        private String id;
        private String name;
    }
}
