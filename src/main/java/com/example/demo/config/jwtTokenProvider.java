//package com.example.demo.config;
//
//import com.example.demo.exception.AppException;
//import com.example.demo.exception.ErrorCode;
//import com.nimbusds.jwt.SignedJWT;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Component;
//
//import java.text.ParseException;
//
//@Component
//@RequiredArgsConstructor
//@Slf4j
//public class jwtTokenProvider {
//    public SignedJWT parseToken(String token) {
//        try {
//            return SignedJWT.parse(token);
//        } catch (ParseException e) {
//            throw new AppException(ErrorCode.UNAUTHENTICATED);
//        }
//    }
//    public String extractUsername(String token) {
//        try {
//            SignedJWT signedJWT = parseToken(token);
//            return signedJWT.getJWTClaimsSet().getSubject();
//        } catch (Exception e) {
//            throw new AppException(ErrorCode.UNAUTHENTICATED);
//        }
//    }
//}
