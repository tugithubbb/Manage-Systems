package com.example.demo.controller;


import com.example.demo.dto.request.*;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.dto.response.MyInfoResponse;
import com.example.demo.dto.response.UserResponse;
import com.example.demo.services.AdminUserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.List;

@RestController
@RequestMapping("/v1/admin/admin_user")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AdminUserController {
    AdminUserService adminUserService;

    @PostMapping("/registration")
    ApiResponse<UserResponse> createUser(@RequestBody @Valid UserCreateRequest request) {
        log.info("Request được xử lý bởi thread: {}", Thread.currentThread());
        return ApiResponse.<UserResponse>builder()
                .result(adminUserService.createUser(request))
                .build();
    }
    @GetMapping("/check-thread")
    public String checkThread() throws InterruptedException {
        System.out.println("Thread info: " + Thread.currentThread());
        Thread.sleep(2000);
        return "OK";
    }
    @GetMapping("/me")
    ApiResponse<MyInfoResponse> getMyInfo(HttpServletRequest request) throws ParseException {
        return ApiResponse.<MyInfoResponse>builder()
                .result(adminUserService.getMyInfo(request))
                .build();
    }

    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<AdminUserDTO>>> getAllAdminUsers() {
        try {
            List<AdminUserDTO> adminUsers = adminUserService.getAllAdminUsers();
            return ResponseEntity.ok(
                    ApiResponse.<List<AdminUserDTO>>builder()
                            .result(adminUsers)
                            .message("Lấy danh sách admin user thành công")
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<List<AdminUserDTO>>builder()
                            .success(false)
                            .message("Lỗi: " + e.getMessage())
                            .build()
                    );
        }
    }

    @GetMapping("/detail/{id}")
    public ResponseEntity<ApiResponse<AdminUserDTO>> getAdminUserDetail(@PathVariable String id) {
        try {
            AdminUserDTO adminUser = adminUserService.getAdminUserById(id);
            return ResponseEntity.ok(
                    ApiResponse.<AdminUserDTO>builder()
                            .success(true)
                            .message("Lấy chi tiết admin user thành công")
                            .result(adminUser)
                            .build()
            );
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.<AdminUserDTO>builder()
                            .success(false)
                            .message(e.getMessage())
                            .build()
                    );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<AdminUserDTO>builder()
                            .success(false)
                            .message("Lỗi: " + e.getMessage())
                            .build()
                    );
        }
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<AdminUserDTO>> createAdminUser(
            @RequestBody AdminUserCreateRequest request) {
        try {
            AdminUserDTO created = adminUserService.createAdminUser(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.<AdminUserDTO>builder()
                            .success(true)
                            .message("Tạo admin user thành công")
                            .result(created)
                            .build()
                    );
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<AdminUserDTO>builder()
                            .success(false)
                            .message(e.getMessage())
                            .build()
                    );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<AdminUserDTO>builder()
                            .success(false)
                            .message("Lỗi: " + e.getMessage())
                            .build()
                    );
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse<AdminUserDTO>> updateAdminUser(
            @PathVariable String id,
            @RequestBody AdminUserUpdateRequest request) {
        try {
            AdminUserDTO updated = adminUserService.updateAdminUser(id, request);
            return ResponseEntity.ok(
                    ApiResponse.<AdminUserDTO>builder()
                            .success(true)
                            .message("Cập nhật admin user thành công")
                            .result(updated)
                            .build()
            );
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<AdminUserDTO>builder()
                            .success(false)
                            .message(e.getMessage())
                            .build()
                    );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<AdminUserDTO>builder()
                            .success(false)
                            .message("Lỗi: " + e.getMessage())
                            .build()
                    );
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAdminUser(@PathVariable String id) {
        try {
            adminUserService.deleteAdminUser(id);
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

    @PostMapping("/assign-role")
    public ResponseEntity<ApiResponse<AdminUserDTO>> assignRole(
            @RequestBody AssignRoleRequest request) {
        try {
            AdminUserDTO updated = adminUserService.assignRole(request);
            return ResponseEntity.ok(
                    ApiResponse.<AdminUserDTO>builder()
                            .success(true)
                            .message("Gán quyền cho admin user thành công")
                            .result(updated)
                            .build()
            );
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<AdminUserDTO>builder()
                            .success(false)
                            .message(e.getMessage())
                            .build()
                    );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<AdminUserDTO>builder()
                            .success(false)
                            .message("Lỗi: " + e.getMessage())
                            .build()
                    );
        }
    }

}


//    @GetMapping
//    ApiResponse<List<UserResponse>> getUsers() {
//        return ApiResponse.<List<UserResponse>>builder()
//                .result(userService.getUsers())
//                .build();
//    }
//
//    @GetMapping("/{userId}")
//    ApiResponse<UserResponse> getUser(@PathVariable("userId") String userId) {
//        return ApiResponse.<UserResponse>builder()
//                .result(userService.getUser(userId))
//                .build();
//    }