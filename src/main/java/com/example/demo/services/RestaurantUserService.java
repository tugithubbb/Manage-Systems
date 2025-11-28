package com.example.demo.services;

import com.example.demo.Entity.*;
import com.example.demo.dto.request.*;
import com.example.demo.dto.response.AuthResponse;
import com.example.demo.dto.response.LoginResponse;
import com.example.demo.dto.response.RestaurantUserResponse;
import com.example.demo.dto.response.UserProfileResponse;
import com.example.demo.exception.AppException;
import com.example.demo.exception.BusinessException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.mapper.RestaurantUserMapper;
import com.example.demo.repository.*;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static com.example.demo.services.AuthenticationService.macSigner;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RestaurantUserService {

    @NonFinal
    @Value("${jwt.signerKey}")
    protected String SIGNER_KEY;

    @NonFinal
    @Value("${jwt.valid-duration}")
    protected long VALID_DURATION;

    @NonFinal
    @Value("${jwt.refreshable-duration}")
    protected long REFRESHABLE_DURATION;
    RestaurantUserRepository restaurantUserRepository;
    RoleRepository roleRepository;
    RestaurantRepository restaurantRepository;
    RestaurantUserMapper userMapper;
    PasswordEncoder passwordEncoder;
    LoginHistoryRepository loginHistoryRepository;
    RefreshTokenRepository refreshTokenRepository;
    @Transactional
    public LoginResponse login(LoginRequest request, HttpServletRequest httpServletRequest) throws JOSEException {
        RestaurantUser restaurantUser = restaurantUserRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));

        if (!passwordEncoder.matches(request.getPassword(), restaurantUser.getPassword())) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        // new token
        AuthResponse access = generateAccessToken(restaurantUser);
        AuthResponse refresh = generateRefreshToken(restaurantUser);
        // log RT khi lưu
        log.info("Saving RT for user {}", restaurantUser.getId());
        log.info("RT value saving = {}", refresh.getToken());

        refreshTokenRepository.deleteByRestaurantUser(restaurantUser);
        refreshTokenRepository.flush();
        // save refreshtoken
        RefreshToken refreshToken = RefreshToken.builder()
                .restaurantUser(restaurantUser)
                .token(refresh.getToken())
                .expiryTime(refresh.getExpiryTime())
                .revoked(false)
                .build();
        refreshTokenRepository.save(refreshToken);

        return LoginResponse.builder()
                .accessToken(access.getToken())
                .refreshToken(refresh.getToken())
                .expiryTime(access.getExpiryTime())
                .build();
    }

    public AuthResponse generateAccessToken(RestaurantUser restaurantUser) throws JOSEException {
        log.info("🔍 VALID_DURATION value: {}", VALID_DURATION);
        return generateToken(restaurantUser, VALID_DURATION);

    }

    public AuthResponse generateRefreshToken(RestaurantUser restaurantUser) throws JOSEException {
        log.info("🔍 REFRESHABLE_DURATION value: {}", REFRESHABLE_DURATION);
        return generateToken(restaurantUser, REFRESHABLE_DURATION);
    }

    private AuthResponse generateToken(RestaurantUser restaurantUser, long durationSeconds) throws JOSEException {
        Instant now = Instant.now();
        Instant expiration = now.plus(durationSeconds, ChronoUnit.SECONDS);
        SignedJWT signedJWT = null;
        try {
            JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.HS512)
                    .type(JOSEObjectType.JWT)
                    .build();

            String roles = restaurantUser.getRole() != null ? restaurantUser.getRole().getCode() : "User";

            List<String> permissions = restaurantUser.getRole() != null
                    ? restaurantUser.getRole().getPermissions().stream()
                    .map(Permission::getCode)
                    .collect(Collectors.toList())
                    : Collections.emptyList();
            RestaurantUser restUser = restaurantUserRepository.findByUsername(restaurantUser.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            String restaurantId = restUser.getRestaurant().getId();

            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .subject(restaurantUser.getUsername())
                    .issuer("maingoctu.com")
                    .issueTime(new Date())
                    .expirationTime(Date.from(expiration))
                    .jwtID(UUID.randomUUID().toString())
                    .claim("scope", "USER")
                    .claim("user_id", restaurantUser.getId())
                    .claim("restaurant_id", restaurantId)
                    .claim("role", roles)
                    .claim("permissions", permissions)
                    .build();
            signedJWT = new SignedJWT(header, claims);
            signedJWT.sign(getMacSigner());
            log.info("✅ Token generated successfully, JTI: {}", claims.getJWTID());
        } catch (JOSEException e) {
            log.error("Failed to generate {} token for user: {}",
                    restaurantUser.getId(), e);
        }
        return AuthResponse.builder()
                .token(signedJWT.serialize())
                .expiryTime(expiration)
                .build();
    }
    private MACSigner getMacSigner() {
        if (macSigner == null) {
            synchronized (this) {
                if (macSigner == null) {
                    try {
                        macSigner = new MACSigner(SIGNER_KEY.getBytes(StandardCharsets.UTF_8));
                    } catch (JOSEException e) {
                        log.error("Failed to initialize MAC signer", e);
                        throw new IllegalStateException("Invalid signer key configuration", e);
                    }
                }
            }
        }
        return macSigner;
    }
    @Transactional
    public void logout(HttpServletRequest request) throws ParseException, JOSEException {
        String username = (String) request.getAttribute("username");

        if (username == null) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        log.info("Logging out user: {}", username);

        RestaurantUser restaurantUser = restaurantUserRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        refreshTokenRepository.revokeALLTokensByUser(restaurantUser.getId());
    }

    public void changePassword(ChangePasswordRequest request, String userId) throws BadRequestException {
        RestaurantUser restaurantUser = restaurantUserRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        if(!passwordEncoder.matches(request.getCurrentPassword(),restaurantUser.getPassword())){
            throw new BadRequestException("Current password is incorrect");
        }
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("New password and confirm password do not match");
        }
        if (request.getNewPassword().equals(request.getCurrentPassword())) {
            throw new BadRequestException("New password must be different from current password");
        }
        //update password
        restaurantUser.setPassword(passwordEncoder.encode(request.getNewPassword()));
        restaurantUserRepository.save(restaurantUser);
        log.info("Password changed successfully for user: {}", restaurantUser.getUsername());
    }

    @Transactional
    public RestaurantUserResponse createUser(CreateRestaurantUserRequest request) throws BusinessException {
        log.info("Creating restaurant user with username: {}", request.getUsername());

        if (restaurantUserRepository.existsByUsernameAndDeletedAtIsNull(request.getUsername())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new BusinessException("Role không tồn tại"));

        Restaurant restaurant = restaurantRepository.findById(request.getRestaurantId())
                .orElseThrow(() -> new BusinessException("Restaurant không tồn tại"));

        RestaurantUser user = RestaurantUser.builder()
                .username(request.getUsername())
                .role(role)
                .restaurant(restaurant)
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        user = restaurantUserRepository.save(user);
        log.info("Created restaurant user: {} for restaurant: {}", user.getUsername(), restaurant.getName());

        return userMapper.toResponse(user);
    }


    public UserProfileResponse getUserById(String id) {
        log.info("Fetching restaurant user with id: {}", id);

        RestaurantUser user = restaurantUserRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Restaurant restaurant = restaurantRepository.findById(user.getRestaurant().getId())
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));

        UserProfileResponse.RestaurantDTO restaurantDTO =
                new UserProfileResponse.RestaurantDTO(
                        restaurant.getId(),
                        restaurant.getName(),
                        restaurant.getAddress()
                );


        return new UserProfileResponse(
                user.getId(),
                user.getUsername(),
                restaurantDTO
        );
    }


    public List<RestaurantUserResponse> getAllUsers() {
        return restaurantUserRepository.findAll()
                    .stream()
                    .map(userMapper::toResponse)
                    .collect(Collectors.toList());
    }
    @Transactional
    public void deleteUser(String id) {
        log.info("Soft deleting restaurant user with id: {}", id);

        RestaurantUser user = restaurantUserRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        user.setDeletedAt(LocalDateTime.now());
        restaurantUserRepository.save(user);

        log.info("Restaurant user soft deleted successfully with id: {}", id);
    }
    public Optional<RestaurantUser> handleGetUserByUsername(String username) {
        return this.restaurantUserRepository.findByUsername(username);
    }
    @Transactional
    public RestaurantUserResponse updateUser(String id, UpdateRestaurantUserRequest request) {
        log.info("Updating restaurant user with id: {}", id);

        RestaurantUser user = restaurantUserRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (request.getUsername() != null && !request.getUsername().equals(user.getUsername())) {
            if (restaurantUserRepository.existsByUsernameAndDeletedAtIsNull(request.getUsername())) {
                throw new AppException(ErrorCode.USER_EXISTED);
            }
            user.setUsername(request.getUsername());
        }


        if (request.getRoleId() != null) {
            Role role = roleRepository.findById(request.getRoleId())
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
            user.setRole(role);
        }

        if (request.getRestaurantId() != null) {
            Restaurant restaurant = restaurantRepository.findById(request.getRestaurantId())
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
            user.setRestaurant(restaurant);
        }

        RestaurantUser updatedUser = restaurantUserRepository.save(user);
        log.info("Restaurant user updated successfully with id: {}", id);

        return userMapper.toResponse(updatedUser);
    }

}
