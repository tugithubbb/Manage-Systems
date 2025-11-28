package com.example.demo.config;

import com.example.demo.Entity.AdminUser;
import com.example.demo.Entity.Permission;
import com.example.demo.Entity.RestaurantUser;
import com.example.demo.Entity.Role;
import com.example.demo.exception.PermissionException;
import com.example.demo.services.AdminUserService;
import com.example.demo.services.RestaurantUserService;
import com.example.demo.util.JsonLogger;
import com.example.demo.util.SecurityUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.server.Jsp;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Slf4j
public class PermissionInterceptor implements HandlerInterceptor {
    @Autowired
    AdminUserService adminUserService;

    @Autowired
    RestaurantUserService restaurantUserService;

    private static final Set<String> PUBLIC_ENDPOINTS = Set.of(
            "/v1/admin/restaurant-user/me",
            "/v1/admin/restaurant-user/change-password",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/swagger-ui.html",
            "/swagger-resources/**",
            "/webjars/**",
            "/v3/api-docs.yaml"

    );
    @Override
    @Transactional
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler) throws Exception {

        long startTime = System.currentTimeMillis();
        request.setAttribute("startTime", startTime);

        String path = (String) request.getAttribute(
                HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE
        );
        String requestURI = request.getRequestURI();
        String httpMethod = request.getMethod();
        AntPathMatcher pathMatcher = new AntPathMatcher();
        // === LOG: Incoming request ===
        JsonLogger.info("PermissionInterceptor: Checking permission", Map.of(
                "method", httpMethod,
                "path", path != null ? path : requestURI,
                "requestURI", requestURI
        ));
        // =============== PUBLIC ENDPOINT ================

        for(String pattern : PUBLIC_ENDPOINTS){
            if(pathMatcher.match(pattern,requestURI)){
                JsonLogger.info("PermissionInterceptor: Public endpoint allowed", Map.of(
                        "requestURI", requestURI,
                        "pattern", pattern
                ));
                return true;
            }
        }
//        if (PUBLIC_ENDPOINTS.contains(path)) {
//            log.info(">>> PUBLIC ENDPOINT ACCESS GRANTED: {}", path);
//            return true;
//        }

        // Get current user
        String username = SecurityUtil.getCurrentUserLogin().orElse("");
        log.info(">>> 🔍 Email from JWT: [{}]", username);

        if (username.isEmpty()) {
            JsonLogger.error("PermissionInterceptor: User not logged in", Map.of(
                    "requestURI", requestURI,
                    "duration", (System.currentTimeMillis() - startTime) + "ms"
            ));
            throw new PermissionException("Người dùng chưa đăng nhập.");
        }
        JsonLogger.debug("PermissionInterceptor: Checking user permissions", Map.of(
                "username", username,
                "path", path
        ));

        Optional<AdminUser> adminUser = this.adminUserService.handleGetUserByUsername(username);
        Optional<RestaurantUser> restaurantOpt = Optional.empty();
        if (adminUser.isEmpty()) {
            JsonLogger.debug("PermissionInterceptor: Admin user not found, checking restaurant user", Map.of(
                    "username", username
            ));
            restaurantOpt = this.restaurantUserService.handleGetUserByUsername(username);
        }
        // 3️⃣ User not found
        if (adminUser.isEmpty() && restaurantOpt.isEmpty()) {
            JsonLogger.error("PermissionInterceptor: User not found in database", Map.of(
                    "username", username,
                    "duration", (System.currentTimeMillis() - startTime) + "ms"
            ));
            throw new PermissionException("Người dùng không tồn tại.");
        }

        Role role;
        if (adminUser.isPresent()) {
            role = adminUser.get().getRole();
            JsonLogger.debug("PermissionInterceptor: Admin user found", Map.of(
                    "username", username,
                    "role", role != null ? role.getName() : "null"
            ));
        } else {
            role = restaurantOpt.get().getRole();
            JsonLogger.debug("PermissionInterceptor: Restaurant user found", Map.of(
                    "username", username,
                    "role", role != null ? role.getName() : "null"
            ));
        }

        if (role == null) {
            JsonLogger.error("PermissionInterceptor: User has no role", Map.of(
                    "username", username,
                    "duration", (System.currentTimeMillis() - startTime) + "ms"
            ));
            throw new PermissionException("Người dùng không có role.");
        }

        List<Permission> permissions = role.getPermissions();
        if (permissions == null || permissions.isEmpty()) {
            JsonLogger.warn("PermissionInterceptor: Role has no permissions", Map.of(
                    "username", username,
                    "role", role.getName(),
                    "duration", (System.currentTimeMillis() - startTime) + "ms"
            ));
            throw new PermissionException("Role không có quyền truy cập.");
        }

        // === LOG: User permissions (debug mode) ===
        if (log.isDebugEnabled()) {
            JsonLogger.debug("PermissionInterceptor: User permissions loaded", Map.of(
                    "username", username,
                    "role", role.getName(),
                    "permissions_count", permissions.size(),
                    "permissions", permissions.stream()
                            .map(p -> Map.of("method", p.getMethod(), "path", p.getApiPath()))
                            .toList()
            ));
        }


        boolean isAllow = permissions.stream().anyMatch(item -> {
            boolean methodMatch = item.getMethod().equalsIgnoreCase(httpMethod);
            boolean pathMatch = pathMatcher.match(item.getApiPath(), path);

            if (methodMatch && pathMatch) {
                JsonLogger.debug("PermissionInterceptor: Permission matched", Map.of(
                        "permission_method", item.getMethod(),
                        "permission_path", item.getApiPath(),
                        "request_method", httpMethod,
                        "request_path", path
                ));
            }

            return methodMatch && pathMatch;
        });

        if (!isAllow) {
            JsonLogger.warn("PermissionInterceptor: Access denied - No matching permission", Map.of(
                    "username", username,
                    "role", role.getName(),
                    "method", httpMethod,
                    "path", path,
                    "requestURI", requestURI,
                    "duration", (System.currentTimeMillis() - startTime) + "ms"
            ));
            throw new PermissionException(
                    "Bạn không có quyền truy cập endpoint này."
            );
        }

        // === LOG: Access granted ===
        JsonLogger.info("PermissionInterceptor: Access granted", Map.of(
                "username", username,
                "role", role.getName(),
                "method", httpMethod,
                "path", path,
                "duration", (System.currentTimeMillis() - startTime) + "ms"
        ));
        return true;

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        try {
            long startTime = (long) request.getAttribute("startTime");
            long duration = System.currentTimeMillis() - startTime;
            int status = response.getStatus();

            // === LOG: Response sent ===
            if (status >= 200 && status < 300) {
                JsonLogger.info("PermissionInterceptor: Request completed successfully", Map.of(
                        "http_status", status,
                        "duration", duration + "ms"
                ));
            } else if (status >= 400) {
                JsonLogger.warn("PermissionInterceptor: Request completed with error status", Map.of(
                        "http_status", status,
                        "duration", duration + "ms"
                ));
            }

            // === LOG TẠI ĐIỀU KIỆN: Exception occurred ===
            if (ex != null) {
                JsonLogger.error("PermissionInterceptor: Exception during request processing", Map.of(
                        "error_message", ex.getMessage(),
                        "error_class", ex.getClass().getSimpleName(),
                        "http_status", status,
                        "duration", duration + "ms"
                ), ex);
            }

        } catch (Exception e) {
            // LOG ERROR: Failed to log in afterCompletion
            JsonLogger.error("PermissionInterceptor: Failed to log in afterCompletion", Map.of(
                    "error_message", e.getMessage()
            ), e);
        }
    }
}