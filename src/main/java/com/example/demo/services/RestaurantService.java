package com.example.demo.services;

import com.example.demo.Entity.AdminUser;
import com.example.demo.Entity.Restaurant;
import com.example.demo.Entity.RestaurantUser;
import com.example.demo.dto.request.RestaurantCreateRequest;
import com.example.demo.dto.response.RestaurantResponse;
import com.example.demo.dto.response.UserResponse;
import com.example.demo.exception.PermissionException;
import com.example.demo.mapper.RestaurantMapper;
import com.example.demo.repository.AdminUserRepository;
import com.example.demo.repository.RestaurantRepository;
import com.example.demo.repository.RestaurantUserRepository;
import com.example.demo.util.JsonLogger;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Service
public class RestaurantService {
    RestaurantRepository restaurantRepository;
    RestaurantMapper restaurantMapper;
    AdminUserRepository adminUserRepository;
    RestaurantUserRepository restaurantUserRepository;

    public List<RestaurantResponse> getAllRestaurants() {
        return restaurantRepository.findAll().stream()
                .map(restaurantMapper::toRestaurantResponse)
                .collect(Collectors.toList());
    }

    public Optional<RestaurantResponse> getRestaurantById(String id, @AuthenticationPrincipal Jwt jwt) throws PermissionException {
        long startTime = System.currentTimeMillis();
        String userId = jwt.getClaimAsString("user_id");
        // Log đầu vào method
        // LOG: Permission denied
        JsonLogger.warn("Service: Restaurant access denied", Map.of(
                "user_id", userId,
                "restaurant_id", id,
                "reason", "User does not have access to this restaurant"
        ));
        try{
            restaurantUserRepository.findById(userId)
                    .filter(user -> user.getRestaurant().getId().equals(id))
                    .orElseThrow(() -> {
                        // LOG: Permission denied
                        JsonLogger.warn("Service: Restaurant access denied", Map.of(
                                "user_id", userId,
                                "restaurant_id", id,
                                "reason", "User does not have access to this restaurant"
                        ));
                        return new PermissionException("Unauthorized");
                    });

            Optional<RestaurantResponse> result = restaurantRepository.findById(id)
                    .map(restaurantMapper::toRestaurantResponse);

            // LOG TẠI ĐIỀU KIỆN: Restaurant not found
            if (result.isEmpty()) {
                JsonLogger.warn("Service: Restaurant not found", Map.of(
                        "restaurant_id", id,
                        "user_id", userId
                ));
            }

            return result;

        }catch (Exception e){
            JsonLogger.error("Service: Error getting restaurant by ID", Map.of(
                    "error_message", e.getMessage(),
                    "user_id", userId,
                    "restaurant_id", id
            ), e);
            throw e;
        }


//        return restaurantRepository.findById(id).map(restaurantMapper::toRestaurantResponse);
    }

    public RestaurantResponse createRestaurant(RestaurantCreateRequest dto) {
        Restaurant restaurant = restaurantMapper.toRestaurant(dto);

        Restaurant saved = restaurantRepository.save(restaurant);

        return restaurantMapper.toRestaurantResponse(saved);
    }

    public Optional<RestaurantResponse> updateRestaurant(String restaurantId, RestaurantCreateRequest restaurantCreateRequest) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> {
                    // LOG TẠI ĐIỀU KIỆN: Restaurant not found for update
                    JsonLogger.warn("Service: Restaurant not found for update", Map.of(
                            "restaurant_id", restaurantId
                    ));
                    return new RuntimeException("Restaurant not found");
                });

        // Cập nhật thông tin
        restaurant.setName(restaurantCreateRequest.getName());
        restaurant.setAddress(restaurantCreateRequest.getAddress());

        Restaurant updated = restaurantRepository.save(restaurant);
        // LOG: Restaurant updated successfully
        JsonLogger.info("Service: Restaurant updated successfully", Map.of(
                "restaurant_id", updated.getId(),
                "new_name", updated.getName(),
                "new_address", updated.getAddress()
        ));

        return Optional.of(restaurantMapper.toRestaurantResponse(updated));
    }

    public boolean deleteRestaurant(String id) {
        try{
            if (!restaurantRepository.existsById(id)) {
                JsonLogger.warn("Service: Cannot delete - restaurant not found", Map.of(
                        "restaurant_id", id
                ));
                return false;
            }
            restaurantRepository.deleteById(id);
            // LOG: Restaurant deleted successfully
            JsonLogger.info("Service: Restaurant deleted successfully", Map.of(
                    "restaurant_id", id
            ));

            return true;

        }catch (Exception e){
            JsonLogger.error("Service: Failed to delete restaurant", Map.of(
                    "error_message", e.getMessage(),
                    "restaurant_id", id
            ), e);
            throw e;
        }

    }

    }


