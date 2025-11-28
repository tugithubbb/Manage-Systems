package com.example.demo.services;

//import com.example.demo.Entity.InvalidatedToken;
import com.example.demo.Entity.AdminUser;
import com.example.demo.Entity.LoginHistory;
import com.example.demo.Entity.Permission;
import com.example.demo.Entity.RefreshToken;
import com.example.demo.dto.request.LoginRequest;
import com.example.demo.dto.response.AuthResponse;
import com.example.demo.dto.response.IntrospectResponse;
import com.example.demo.dto.response.LoginResponse;
import com.example.demo.exception.AppException;
import com.example.demo.exception.ErrorCode;
//import com.example.demo.repository.InvalidatedTokenRepository;
import com.example.demo.repository.LoginHistoryRepository;
import com.example.demo.repository.RefreshTokenRepository;
import com.example.demo.repository.AdminUserRepository;
import com.example.demo.util.TokenExtractor;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationService {
    AdminUserRepository adminUserRepository;
    PasswordEncoder passwordEncoder;
    LoginHistoryRepository loginHistoryRepository;
    static MACSigner macSigner;
//    InvalidatedTokenRepository invalidatedTokenRepository;
    TokenExtractor tokenExtractor;
    RefreshTokenRepository refreshTokenRepository;

    @NonFinal
    @Value("${jwt.signerKey}")
    protected String SIGNER_KEY;

    @NonFinal
    @Value("${jwt.valid-duration}")
    protected long VALID_DURATION;

    @NonFinal
    @Value("${jwt.refreshable-duration}")
    protected long REFRESHABLE_DURATION;
    public IntrospectResponse introspect(HttpServletRequest request) throws ParseException {
      String token = tokenExtractor.extractToken(request);
        boolean isValid = true;
        SignedJWT jwt = null;

        try {
            jwt = verifyToken(token, false);
            Optional<RefreshToken> optionalToken = refreshTokenRepository.findByToken(token);
            if (optionalToken.isPresent() && optionalToken.get().isRevoked()) {
                log.info("Token introspect failed: revoked");
                isValid = false;
            }
        } catch (AppException | JOSEException | ParseException e) {
            log.info("Token introspect failed: {}", e.getMessage());
            isValid = false;
        }
        return IntrospectResponse.builder()
                .userId(jwt != null ? jwt.getJWTClaimsSet().getSubject() : null)
                .valid(isValid).build();
    }
    @Transactional
    public LoginResponse login(LoginRequest request, HttpServletRequest httpServletRequest) throws JOSEException {
        AdminUser adminUser = adminUserRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));

        if (!passwordEncoder.matches(request.getPassword(), adminUser.getPassword())) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        try {
            saveLoginHistory(adminUser, httpServletRequest);
        } catch (Exception e) {
            e.getMessage();
            log.info("An INFO Message");
        }
        // new token
        AuthResponse access = generateAccessToken(adminUser);
        AuthResponse refresh = generateRefreshToken(adminUser);
        // log RT khi lưu
        log.info("Saving RT for user {}", adminUser.getId());
        log.info("RT value saving = {}", refresh.getToken());

        refreshTokenRepository.deleteByAdminUser(adminUser);
        refreshTokenRepository.flush();
        // save refreshtoken
        RefreshToken refreshToken = RefreshToken.builder()
                .adminUser(adminUser)
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
    public void logout(HttpServletRequest request) throws ParseException, JOSEException {
//        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
//        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
//            throw new AppException(ErrorCode.UNAUTHENTICATED);
//        }
//        String token = authHeader.substring(7);
//
//        SignedJWT signedToken;
//        try {
//            // Try to verify with expiration check
//            signedToken = verifyToken(token, false);
//        } catch (JOSEException e) {
//            // If expired, parse without verification
//            log.info("Token expired, parsing for invalidation");
//            signedToken = SignedJWT.parse(token);
//
//            // Still verify signature
//            JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());
//            if (!signedToken.verify(verifier)) {
//                throw new AppException(ErrorCode.UNAUTHENTICATED);
//            }
//        }
//        String username = signedToken.getJWTClaimsSet().getSubject();
//        User user = userRepository.findByUsername(username)
//                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));
//        refreshTokenRepository.revokeALLTokensByUser(user.getId());
        String username = (String) request.getAttribute("username");

        if (username == null) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        log.info("Logging out user: {}", username);

        AdminUser adminUser = adminUserRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        refreshTokenRepository.revokeALLTokensByUser(adminUser.getId());
    }

    public void saveLoginHistory(AdminUser adminUser, HttpServletRequest request) {
        LoginHistory loginHistory = LoginHistory.builder()
                .adminUser(adminUser)
                .loginTime(LocalDateTime.now())
                .ipAddress(getClientIp(request))
                .deviceInfo(request.getHeader("User-Agent"))
                .build();
        loginHistoryRepository.save(loginHistory);
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    public AuthResponse generateAccessToken(AdminUser adminUser) throws JOSEException {
        log.info("🔍 VALID_DURATION value: {}", VALID_DURATION);
        return generateToken(adminUser, VALID_DURATION);

    }

    public AuthResponse generateRefreshToken(AdminUser adminUser) throws JOSEException {
        log.info("🔍 REFRESHABLE_DURATION value: {}", REFRESHABLE_DURATION);
        return generateToken(adminUser, REFRESHABLE_DURATION);
    }
    private AuthResponse generateToken(AdminUser adminUser, long durationSeconds) throws JOSEException {
        Instant now = Instant.now();
        Instant expiration = now.plus(durationSeconds, ChronoUnit.SECONDS);
        SignedJWT signedJWT = null;
        try {
            JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.HS512)
                    .type(JOSEObjectType.JWT)
                    .build();

            String roles = adminUser.getRole() != null ? adminUser.getRole().getCode() : "User";

            List<String> permissions = adminUser.getRole() != null
                    ? adminUser.getRole().getPermissions().stream()
                    .map(Permission::getCode)
                    .collect(Collectors.toList())
                    : Collections.emptyList();

            List<String> restaurantIds = adminUser.getRestaurants() != null
                    ? adminUser.getRestaurants()
                    .stream()
                    .map(r -> r.getId().toString())
                    .collect(Collectors.toList())
                    : Collections.emptyList();


            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .subject(adminUser.getUsername())
                    .issuer("maingoctu.com")
                    .issueTime(new Date())
                    .expirationTime(Date.from(expiration))
                    .jwtID(UUID.randomUUID().toString())
                    .claim("scope", "USER")
                    .claim("user_id", adminUser.getId())
                    .claim("role", roles)
                    .claim("restaurant_id", restaurantIds)
                    .claim("permissions", permissions)
                    .build();
            signedJWT = new SignedJWT(header, claims);
            signedJWT.sign(getMacSigner());
            log.info("✅ Token generated successfully, JTI: {}", claims.getJWTID());
        } catch (JOSEException e) {
            log.error("Failed to generate {} token for user: {}",
                    adminUser.getId(), e);
        }
        return AuthResponse.builder()
                .token(signedJWT.serialize())
                .expiryTime(expiration)
                .build();
    }

    public LoginResponse refreshToken(HttpServletRequest request) throws JOSEException, ParseException {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new AppException(ErrorCode.INVALID_KEY);
        }
        String refreshTokenValue = authHeader.substring(7);
        // 1. Parse + verify token signature
        SignedJWT signedJWT;
        try {
            signedJWT = verifyToken(refreshTokenValue, true);
            log.info("RT from client sub = {}", signedJWT.getJWTClaimsSet().getSubject());// true = allow refresh validation
        } catch (Exception e) {
            throw new AppException(ErrorCode.INVALID_TOKEN);
        }
        // get Id
        var userId = signedJWT.getJWTClaimsSet().getSubject();
        log.info("username: {}", userId);
        // check RefreshToken in db
        // Log token client gửi lên
        log.info("RT from client: {}", refreshTokenValue);

// Tìm trong DB và log kết quả
        RefreshToken savedToken = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> {
                    log.warn("❌ Refresh token NOT FOUND in DB: {}", refreshTokenValue);
                    return new AppException(ErrorCode.LoginLai);
                });

// Log token tìm được trong DB
        log.info("✅ Found RT in DB: id={}, revoked={}, expiry={}",
                savedToken.getId(),
                savedToken.isRevoked(),
                savedToken.getExpiryTime());

        // Check revoked or  expired
        if (savedToken.isRevoked() || savedToken.getExpiryTime().isBefore(Instant.now())) {
            savedToken.setRevoked(true);
            refreshTokenRepository.save(savedToken);
            throw new AppException(ErrorCode.BAD_REQUEST);
        }
        var user = adminUserRepository.findByUsername(userId)
                .orElseThrow(() ->
                        new AppException(ErrorCode.UNAUTHENTICATED));

        //Generate new tokens
        AuthResponse newAccessToken = generateToken(user, VALID_DURATION);
        AuthResponse newRefreshToken = generateToken(user, REFRESHABLE_DURATION);

        //Revoke old refresh token
        savedToken.setRevoked(true);
        refreshTokenRepository.save(savedToken);

        // save new refresher token
        RefreshToken newRefreshEntity = RefreshToken.builder()
                .adminUser(user)
                .token(newRefreshToken.getToken())
                .expiryTime(newRefreshToken.getExpiryTime())
                .revoked(false)
                .build();
        refreshTokenRepository.save(newRefreshEntity);

        return LoginResponse.builder()
                .accessToken(newAccessToken.getToken())
                .refreshToken(newRefreshToken.getToken())
                .expiryTime(newAccessToken.getExpiryTime())
                .build();
    }
    private SignedJWT verifyToken(String token, boolean isRefresh) throws JOSEException, ParseException {

        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());
        SignedJWT signedJWT = SignedJWT.parse(token);

        // verify signature
        if (!signedJWT.verify(verifier)) {
            log.warn("Signature invalid");
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        // ✅ Nếu KHÔNG phải refresh token → check expiration
        if (!isRefresh && expiryTime.before(new Date())) {
            log.warn("Token expired at {}", expiryTime);
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        // ✅ Nếu là refresh → CHO PHÉP expired
        // Vì bạn chỉ cần đọc subject rồi check DB

        log.info("Token verified OK for user {}", signedJWT.getJWTClaimsSet().getSubject());
        return signedJWT;
    }
    public static Optional<String> getCurrentUserLogin() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        return Optional.ofNullable(extractPrincipal(securityContext.getAuthentication()));
    }
    private static String extractPrincipal(Authentication authentication) {
        if (authentication == null) {
            return null;
        } else if (authentication.getPrincipal() instanceof UserDetails springSecurityUser) {
            return springSecurityUser.getUsername();
        } else if (authentication.getPrincipal() instanceof Jwt jwt) {
            return jwt.getSubject();
        } else if (authentication.getPrincipal() instanceof String s) {
            return s;
        }
        return null;
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

}