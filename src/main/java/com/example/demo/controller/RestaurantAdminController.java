package com.example.demo.controller;

import com.example.demo.Entity.RestaurantUser;
import com.example.demo.dto.request.RestaurantCreateRequest;
import com.example.demo.dto.response.RestaurantResponse;
import com.example.demo.exception.PermissionException;
import com.example.demo.repository.RestaurantUserRepository;
import com.example.demo.services.RestaurantService;
import com.example.demo.util.JsonLogger;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/v1/admin/restaurant")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class RestaurantAdminController {
    RestaurantService restaurantService;
    RestaurantUserRepository restaurantUserRepository;
    @GetMapping("/list")
    public List<RestaurantResponse> listAll() {
        long startTime = System.currentTimeMillis();
        List<RestaurantResponse> result = restaurantService.getAllRestaurants();
        return result;
    }

    @GetMapping("/detail/{id}")
    public ResponseEntity<RestaurantResponse> getDetail(@PathVariable String id, @AuthenticationPrincipal Jwt jwt) throws PermissionException {
        long startTime = System.currentTimeMillis();

        var opt = restaurantService.getRestaurantById(id, jwt);
        ResponseEntity<RestaurantResponse> response = opt
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());

        return response;
    }

    @PostMapping("/create")
    public RestaurantResponse create(@RequestBody RestaurantCreateRequest dto) {
        long startTime = System.currentTimeMillis();
        RestaurantResponse response = restaurantService.createRestaurant(dto);
        return response;
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<RestaurantResponse> update(
            @PathVariable("id") String restaurantId,
            @RequestBody RestaurantCreateRequest restaurantCreateRequest) {

        return restaurantService.updateRestaurant(restaurantId, restaurantCreateRequest)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        boolean deleted = restaurantService.deleteRestaurant(id);
        if (!deleted) return ResponseEntity.notFound().build();
        return ResponseEntity.noContent().build();
    }
}
