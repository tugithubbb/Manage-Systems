package com.example.demo.mapper;

import com.example.demo.Entity.Restaurant;
import com.example.demo.dto.request.RestaurantCreateRequest;
import com.example.demo.dto.response.RestaurantResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface RestaurantMapper {
    RestaurantResponse toRestaurantResponse(Restaurant restaurant);
    Restaurant toRestaurant(RestaurantCreateRequest restaurantCreateRequest);
    void updateRestaurantFromDTO(RestaurantCreateRequest restaurantCreateRequest, @MappingTarget Restaurant restaurant);
}
