package com.example.demo.services;

import com.example.demo.Entity.AdminUser;
import com.example.demo.dto.response.UserInfoResponse;
import com.example.demo.exception.AppException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.mapper.UserMapper;
import com.example.demo.repository.AdminUserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
    public class InternalApiService {
    AdminUserRepository adminUserRepository;
    UserMapper userMapper;

        public UserInfoResponse getUserInfo(String userId){
            log.info("Fetching user info for userId: {}", userId);
            AdminUser adminUser = adminUserRepository.findById(userId)
                    .orElseThrow(() -> {
                        log.warn("User not found with id: {}", userId);
                        return new AppException(ErrorCode.USER_NOT_FOUND);
                    });
            log.info("User info fetched successfully for userId: {}", userId);
            return userMapper.toUserInfoResponse(adminUser);
        }

}
