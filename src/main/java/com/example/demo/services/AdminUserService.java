package com.example.demo.services;

import com.example.demo.Entity.AdminUser;
//import com.example.demo.config.JwtAuthenticationFilter;
import com.example.demo.Entity.Role;
import com.example.demo.cache.CacheKey;
import com.example.demo.cache.RedisService;
import com.example.demo.dto.request.*;
import com.example.demo.dto.response.MyInfoCache;
import com.example.demo.dto.response.MyInfoResponse;
import com.example.demo.dto.response.UserResponse;
import com.example.demo.exception.AppException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.mapper.UserMapper;
import com.example.demo.repository.AdminUserRepository;
import com.example.demo.repository.RoleRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AdminUserService {
    AdminUserRepository adminUserRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;
    RoleRepository roleRepository;
    RedisService redisService;
//    JwtAuthenticationFilter jwtAuthenticationFilter;
    @NonFinal
    @Value("${jwt.signerKey}")
    protected String SIGNER_KEY;


    public UserResponse createUser(UserCreateRequest request) {
//        try {
//            Thread.sleep(30000); // giả lập chờ xử lý
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//            throw new RuntimeException(e);
//        }
//        if(userRepository.existsByUsername(request.getUsername())){
//            throw new RuntimeException("Username already exists");
//        }
//        if (userRepository.existsByEmail(request.getEmail())) {
//            throw new RuntimeException("Email already exists");
//        }
//
//        User user = userMapper.toUser(request);
//
//        user.setPassword(passwordEncoder.encode(request.getPassword()));
//
//        try {
//            User savedUser = userRepository.save(user);
//            return userMapper.toUserResponse(savedUser);
//        } catch (Exception e) {
//            log.error("Failed to create user: {}", e.getMessage(), e);
//            throw new RuntimeException("User already exists");
//        }
//    }

        if (log.isDebugEnabled()) {
            Thread currentThread = Thread.currentThread();
            log.debug("Processing on {}, isVirtual: {}",
                    currentThread.getName(),
                    currentThread.isVirtual());
        }

//        try {
//
//            Thread.sleep(30000); // giả lập chờ xử lý 30 giây
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//            log.error("Request interrupted for user: {}", request.getUsername());
//            throw new RuntimeException("Operation interrupted", e);
//        }


//        if (userRepository.existsByUsername(request.getUsername())) {
//            throw new RuntimeException("Username already exists");
//        }
//        if (userRepository.existsByEmail(request.getEmail())) {
//            throw new RuntimeException("Email already exists");
//        }

        // Create and save user
        AdminUser adminUser = userMapper.toUser(request);
        adminUser.setPassword(passwordEncoder.encode(request.getPassword()));

        try {
            AdminUser savedAdminUser = adminUserRepository.save(adminUser);
            log.info("User created successfully: {}", savedAdminUser.getUsername());
            return userMapper.toUserResponse(savedAdminUser);
        } catch (Exception e) {
            log.error("Failed to create user: {}", e.getMessage(), e);
            throw new RuntimeException("User already exists");
        }
    }

//    @Cacheable(
//            value = "my-info",
//            key = "#userId"
//    )
    public MyInfoCache getMyInfoCache(String userId) throws ParseException {
        String key = CacheKey.myInfo(userId);

        MyInfoCache cache = redisService.get(key,MyInfoCache.class);
        if (cache != null) {
            log.info("CACHE HIT my-info {}", userId);
            return cache;
        }
        // Cache miss => query db
        AdminUser adminUser = adminUserRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        MyInfoCache result = new MyInfoCache(
                adminUser.getId(),
                adminUser.getUsername(),
                adminUser.getEmail(),
                adminUser.getFullname(),
                adminUser.getPhone(),
                adminUser.getCreatedAt()
        );
        //set cache
        redisService.set(key,result, Duration.ofMinutes(10));
        log.info("CACHE SET my-info {}", userId);
        return result;
    }

    public MyInfoResponse getMyInfo(String userId) throws ParseException {
        MyInfoCache cache = getMyInfoCache(userId);

        return new MyInfoResponse(
                cache.id(),
                cache.username(),
                cache.email(),
                cache.fullName(),
                cache.phoneNumber(),
                cache.createdAt()
        );
    }
    public Optional<AdminUser> handleGetUserByUsername(String username) {
        return this.adminUserRepository.findByUsername(username);
    }


    public List<AdminUserDTO> getAllAdminUsers() {
        return adminUserRepository.findAllActive()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public AdminUserDTO getAdminUserById(String id) {
        AdminUser adminUser = adminUserRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new RuntimeException("Admin user không tồn tại"));
        return convertToDTO(adminUser);
    }

    @Transactional
    public AdminUserDTO createAdminUser(AdminUserCreateRequest request) {
        if (adminUserRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username đã tồn tại");
        }

        if (adminUserRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email đã tồn tại");
        }

        Role role = null;
        if (request.getRoleId() != null) {
            role = roleRepository.findById(request.getRoleId())
                    .orElseThrow(() -> new RuntimeException("Role không tồn tại"));
        }

        AdminUser adminUser = new AdminUser();
        adminUser.setUsername(request.getUsername());
        adminUser.setFullname(request.getFullname());
        adminUser.setEmail(request.getEmail());
        adminUser.setPassword(passwordEncoder.encode(request.getPassword()));
        adminUser.setPhone(request.getPhone());
        adminUser.setAddress(request.getAddress());
        adminUser.setRole(role);

        AdminUser saved = adminUserRepository.save(adminUser);
        return convertToDTO(saved);
    }

    @Transactional
//    @CacheEvict(value = "my-info", key = "#id")
    public AdminUserDTO updateAdminUser(String id, AdminUserUpdateRequest request) {
        AdminUser adminUser = adminUserRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new RuntimeException("Admin user không tồn tại"));

        if (request.getEmail() != null && !request.getEmail().equals(adminUser.getEmail())) {
            if (adminUserRepository.existsByEmail(request.getEmail())) {
                throw new RuntimeException("Email đã tồn tại");
            }
            adminUser.setEmail(request.getEmail());
        }

        if (request.getFullname() != null) {
            adminUser.setFullname(request.getFullname());
        }

        if (request.getPhone() != null) {
            adminUser.setPhone(request.getPhone());
        }

        if (request.getAddress() != null) {
            adminUser.setAddress(request.getAddress());
        }

        AdminUser updated = adminUserRepository.save(adminUser);
        String key = CacheKey.myInfo(id);
        redisService.delete(key);
        return convertToDTO(updated);
    }

    @Transactional
    public void deleteAdminUser(String id) {
        AdminUser adminUser = adminUserRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new RuntimeException("Admin user không tồn tại"));

        adminUser.setDeletedAt(LocalDateTime.now());
        adminUserRepository.save(adminUser);
    }

    @Transactional
    public AdminUserDTO assignRole(AssignRoleRequest request) {
        AdminUser adminUser = adminUserRepository.findByIdAndNotDeleted(request.getAdminUserId())
                .orElseThrow(() -> new RuntimeException("Admin user không tồn tại"));

        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new RuntimeException("Role không tồn tại"));

        adminUser.setRole(role);
        AdminUser updated = adminUserRepository.save(adminUser);
        return convertToDTO(updated);
    }

    private AdminUserDTO convertToDTO(AdminUser adminUser) {
        return AdminUserDTO.builder()
                .id(adminUser.getId())
                .username(adminUser.getUsername())
                .fullname(adminUser.getFullname())
                .email(adminUser.getEmail())
                .phone(adminUser.getPhone())
                .address(adminUser.getAddress())
                .roleId(adminUser.getRole() != null ? adminUser.getRole().getId() : null)
                .roleName(adminUser.getRole() != null ? adminUser.getRole().getName() : null)
                .createdAt(adminUser.getCreatedAt())
                .updatedAt(adminUser.getUpdatedAt())
                .build();
    }

    /// test

    }



