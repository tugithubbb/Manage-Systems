package com.example.demo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileResponse {
    private String id;
    private String username;
    private RestaurantDTO restaurant;


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RestaurantDTO {
        private String id;
        private String name;
        private String address;
    }

}
