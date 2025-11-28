package com.example.demo.controller;

import com.example.demo.dto.request.IntrospectRequest;
import com.example.demo.dto.request.LoginRequest;
import com.example.demo.dto.request.LogoutRequest;
import com.example.demo.dto.request.RefreshRequest;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.dto.response.AuthResponse;
import com.example.demo.dto.response.IntrospectResponse;
import com.example.demo.dto.response.LoginResponse;
import com.example.demo.services.AuthenticationService;
import com.nimbusds.jose.JOSEException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationController {
    AuthenticationService authenticationService;
    @PostMapping("/login")
    ApiResponse<LoginResponse> authenticate(@RequestBody LoginRequest request, HttpServletRequest httpServletRequest) throws JOSEException {
        var result = authenticationService.login(request,httpServletRequest);
        return ApiResponse.<LoginResponse>builder().result(result).build();
    }
    @PostMapping("/refresh")
    ApiResponse<LoginResponse> refreshToken(HttpServletRequest request)
            throws ParseException, JOSEException {
        var result = authenticationService.refreshToken(request);
        return ApiResponse.<LoginResponse>builder().result(result).build();
    }
    @PostMapping("/introspect")
    ApiResponse<IntrospectResponse> introspect(HttpServletRequest request) throws ParseException {
        var result = authenticationService.introspect(request);
        return ApiResponse.<IntrospectResponse>builder().result(result).build();
    }
    @PostMapping("/logout")
    ApiResponse<Void> logout( HttpServletRequest request) throws ParseException, JOSEException {
        authenticationService.logout(request);
        return ApiResponse.<Void>builder().build();
    }
}
