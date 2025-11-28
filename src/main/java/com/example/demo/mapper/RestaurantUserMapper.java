package com.example.demo.mapper;

import com.example.demo.Entity.RestaurantUser;
import com.example.demo.dto.response.RestaurantUserResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RestaurantUserMapper {
    RestaurantUserResponse toResponse (RestaurantUser restaurantUser);
}
