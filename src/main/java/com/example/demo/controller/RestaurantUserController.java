package com.example.demo.controller;

import com.example.demo.dto.request.*;
import com.example.demo.dto.response.*;
import com.example.demo.services.RestaurantUserService;
import com.nimbusds.jose.JOSEException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.List;

@RestController
@RequestMapping("/v1/admin/restaurant-user")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class RestaurantUserController {
    RestaurantUserService restaurantUserService;


    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> createAdminUser(
            @RequestBody LoginRequest request, HttpServletRequest httpServletRequest) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.<LoginResponse>builder()
                            .success(true)
                            .message("login thành công")
                            .result(restaurantUserService.login(request,httpServletRequest))
                            .build()
                    );
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<LoginResponse>builder()
                            .success(false)
                            .message(e.getMessage())
                            .build()
                    );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<LoginResponse>builder()
                            .success(false)
                            .message("Lỗi: " + e.getMessage())
                            .build()
                    );
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            @AuthenticationPrincipal Jwt jwt) throws BadRequestException {
        String userId = jwt.getClaimAsString("user_id");

        restaurantUserService.changePassword(request, userId);

        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Password changed successfully")
                .build());
    }

    @PostMapping("/logout")
    ApiResponse<Void> logout( HttpServletRequest request) throws ParseException, JOSEException {
       restaurantUserService.logout(request);
        return ApiResponse.<Void>builder().build();
    }

    @GetMapping("/list")
    public ApiResponse<List<RestaurantUserResponse>> list() {
        List<RestaurantUserResponse> list = restaurantUserService.getAllUsers();

        return ApiResponse.<List<RestaurantUserResponse>>builder()
                .code(1000)
                .message("ok")
                .result(list)
                .build();
    }
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<RestaurantUserResponse>> createAdminUser(
            @RequestBody CreateRestaurantUserRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.<RestaurantUserResponse>builder()
                            .success(true)
                            .message("Tạo admin user thành công")
                            .result(restaurantUserService.createUser(request))
                            .build()
                    );
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<RestaurantUserResponse>builder()
                            .success(false)
                            .message(e.getMessage())
                            .build()
                    );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<RestaurantUserResponse>builder()
                            .success(false)
                            .message("Lỗi: " + e.getMessage())
                            .build()
                    );
        }
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getInfo(@AuthenticationPrincipal Jwt jwt) {
        String id = jwt.getClaimAsString("user_id");
        try {
            return ResponseEntity.ok(
                    ApiResponse.<UserProfileResponse>builder()
                            .success(true)
                            .message("Lấy chi tiết user và nhà hàng thành công")
                            .result(restaurantUserService.getUserById(id))
                            .build()
            );
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.<UserProfileResponse>builder()
                            .success(false)
                            .message(e.getMessage())
                            .build()
                    );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<UserProfileResponse>builder()
                            .success(false)
                            .message("Lỗi: " + e.getMessage())
                            .build()
                    );
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse<RestaurantUserResponse>> updateAdminUser(
            @PathVariable String id,
            @RequestBody UpdateRestaurantUserRequest request) {
        try {
            return ResponseEntity.ok(
                    ApiResponse.<RestaurantUserResponse>builder()
                            .success(true)
                            .message("Cập nhật admin user thành công")
                            .result(restaurantUserService.updateUser(id,request))
                            .build()
            );
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<RestaurantUserResponse>builder()
                            .success(false)
                            .message(e.getMessage())
                            .build()
                    );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<RestaurantUserResponse>builder()
                            .success(false)
                            .message("Lỗi: " + e.getMessage())
                            .build()
                    );
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAdminUser(@PathVariable String id) {
        try {
           restaurantUserService.deleteUser(id);
            return ResponseEntity.ok(
                    ApiResponse.<Void>builder()
                            .success(true)
                            .message("Xóa admin user thành công")
                            .build()
            );
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.<Void>builder()
                            .success(false)
                            .message(e.getMessage())
                            .build()
                    );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<Void>builder()
                            .success(false)
                            .message("Lỗi: " + e.getMessage())
                            .build()
                    );
        }
    }

}
