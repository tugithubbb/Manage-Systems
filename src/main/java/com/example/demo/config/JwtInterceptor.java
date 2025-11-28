//package com.example.demo.config;
//
//import com.example.demo.exception.AppException;
//import com.example.demo.exception.ErrorCode;
//import com.nimbusds.jose.JWSVerifier;
//import com.nimbusds.jose.crypto.MACVerifier;
//import com.nimbusds.jwt.JWTClaimsSet;
//import com.nimbusds.jwt.SignedJWT;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import lombok.experimental.NonFinal;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.HttpHeaders;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.stereotype.Component;
//import org.springframework.web.servlet.HandlerInterceptor;
//
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.List;
//
//@Slf4j
//@Component
//public class JwtInterceptor implements HandlerInterceptor {
//    @NonFinal
//    @Value("${jwt.signerKey}")
//    protected String SIGNER_KEY;
//
//    @Override
//    public boolean preHandle(HttpServletRequest request,
//                             HttpServletResponse response,
//                             Object handler) throws Exception {
//
//        String path = request.getRequestURI();
//
//        // Không validate cho login + refresh + swagger
//        if (path.equals("/auth/login") || path.equals("/auth/refresh") || path.startsWith("/api/v1/") || path.startsWith("/api/v1/permissions")) {
//            return true;
//        }
//
//        String auth = request.getHeader(HttpHeaders.AUTHORIZATION);
//        if (auth == null || !auth.startsWith("Bearer ")) {
//            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing token");
//            return false;
//        }
//
//        String token = auth.substring(7);
//        SignedJWT signedJWT;
//        JWTClaimsSet claims = null;
//        try {
//            // 2. Parse token
//            signedJWT = SignedJWT.parse(token);
//            claims = signedJWT.getJWTClaimsSet();
//        } catch (Exception e) {
//            throw new AppException(ErrorCode.INVALID_TOKEN);
//        }
//
//        try {
//            JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());
//            if (!signedJWT.verify(verifier)) {
//                throw new AppException(ErrorCode.INVALID_TOKEN);
//            }
//
//            Date expiry = signedJWT.getJWTClaimsSet().getExpirationTime();
//            if (expiry.before(new Date())) {
//                throw new AppException(ErrorCode.ACCESS_TOKEN_EXPIRED);
//            }
//            String username = signedJWT.getJWTClaimsSet().getSubject();
//            String role = (String) claims.getClaim("role");
//            List<String> permissions = (List<String>) claims.getClaim("permissions");
//            request.setAttribute("username", username);
//            request.setAttribute("role", role);
//            request.setAttribute("permissions", permissions);
//            log.info("✅ Valid token for user: {}, role: {}, permissions: {}", username, role, permissions);
//
//            List<GrantedAuthority> authorities = new ArrayList<>();
//            if (role != null) authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
//            if (permissions != null) {
//                permissions.forEach(p -> authorities.add(new SimpleGrantedAuthority(p)));
//            }
//            UsernamePasswordAuthenticationToken authToken =
//                    new UsernamePasswordAuthenticationToken(username, null, authorities);
//            SecurityContextHolder.getContext().setAuthentication(authToken);
//
//            // Log để kiểm tra authorities
//            log.info("Authorities: {}", SecurityContextHolder.getContext().getAuthentication().getAuthorities());
//
//        } catch (Exception e) {
//            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Access token expired or invalid");
//            return false;
//        }
//
//        return true;
//    }
//    }