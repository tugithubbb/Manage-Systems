package com.example.demo.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.UUID;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class LoggingAspect {
    private final ObjectMapper objectMapper;

    @Around("@within(org.springframework.web.bind.annotation.RestController) || " +
            "@within(org.springframework.stereotype.Controller)")
    public Object logController(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String methodName = signature.getMethod().getName();
        String className = signature.getDeclaringType().getSimpleName();

        HttpServletRequest request = ((ServletRequestAttributes)
                RequestContextHolder.currentRequestAttributes()).getRequest();

        try {
            log.info("Business logic start");

            Object result = joinPoint.proceed();

            MDC.put("duration", (System.currentTimeMillis() - startTime) + "ms");
            MDC.put("status", "SUCCESS");

            log.info("Business logic success");
            return result;

        } catch (Exception e) {
            MDC.put("duration", (System.currentTimeMillis() - startTime) + "ms");
            MDC.put("status", "ERROR");
            MDC.put("errorClass", e.getClass().getSimpleName());
            MDC.put("errorMessage", e.getMessage());

            log.error("Business logic failed", e);
            throw e;
        }
    }


}
