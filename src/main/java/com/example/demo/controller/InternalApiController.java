package com.example.demo.controller;

import com.example.demo.config.InternalApiConfig;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.dto.response.UserInfoResponse;
import com.example.demo.exception.AppException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.services.InternalApiClient;
import com.example.demo.services.InternalApiService;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@RestController
@RequestMapping("/api/internal")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class InternalApiController {
    InternalApiService internalApiService;
    InternalApiConfig internalApiConfig;
    @GetMapping("/{userId}")
    public ApiResponse<UserInfoResponse> getUserInfo(
            @PathVariable @NotBlank(message = "User ID cannot be blank") String userId,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {

        // Validate Basic Auth
        validateBasicAuth(authHeader);

        log.info("Internal API request accepted for userId={}", userId);
        UserInfoResponse userInfo = internalApiService.getUserInfo(userId);

        return ApiResponse.<UserInfoResponse>builder()
                .code(1000)
                .message("Get user info successfully")
                .result(userInfo)
                .build();
    }
    private void validateBasicAuth(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Basic ")) {
            log.warn("Missing or invalid Authorization header format");
            throw new AppException(ErrorCode.INVALID_TOKEN);
        }

        try {
            String base64Credentials = authHeader.substring(6);
            byte[] decodedBytes = Base64.getDecoder().decode(base64Credentials);
            String credentials = new String(decodedBytes, StandardCharsets.UTF_8);

            String[] parts = credentials.split(":", 2);
            if (parts.length != 2) {
                throw new AppException(ErrorCode.INVALID_TOKEN);
            }

            String username = parts[0];
            String password = parts[1];

            String expectedUsername = internalApiConfig.getUsername();
            String expectedPassword = internalApiConfig.getPassword();

            if (!expectedUsername.equals(username) || !expectedPassword.equals(password)) {
                log.warn("Invalid credentials - username: {}", username);
                log.warn("Invalid credentials - username: {}", password);
                throw new AppException(ErrorCode.UNAUTHORIZED);
            }


            log.debug("Internal API authentication successful for username: {}", username);

        } catch (IllegalArgumentException e) {
            log.error("Failed to decode Basic Auth token", e);
            throw new AppException(ErrorCode.INVALID_TOKEN);
        }
    }
}

