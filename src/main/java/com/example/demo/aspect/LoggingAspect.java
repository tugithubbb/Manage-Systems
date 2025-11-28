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

        // Generate trace ID
        String traceId = UUID.randomUUID().toString();

        try {
            // Set MDC for request
            MDC.put("traceId", traceId);
            MDC.put("httpMethod", request.getMethod());
            MDC.put("uri", request.getRequestURI());
            MDC.put("className", className);
            MDC.put("methodName", methodName);
            MDC.put("type", "API_REQUEST");

            // Capture request body
            Object[] args = joinPoint.getArgs();
            if (args != null && args.length > 0) {
                for (Object arg : args) {
                    if (arg != null && !isSpringType(arg)) {
                        try {
                            String requestBody = objectMapper.writeValueAsString(arg);
                            MDC.put("requestBody", requestBody);
                        } catch (Exception e) {
                            MDC.put("requestBody", arg.toString());
                        }
                        break;
                    }
                }
            }

            log.info("API Request received");

            // Execute method
            Object result = joinPoint.proceed();

            // Log response
            long duration = System.currentTimeMillis() - startTime;
            MDC.put("duration", duration + "ms");
            MDC.put("status", "SUCCESS");
            MDC.put("type", "API_RESPONSE");

            log.info("API Request completed");

            return result;

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;

            MDC.put("duration", duration + "ms");
            MDC.put("status", "ERROR");
            MDC.put("type", "API_ERROR");
            MDC.put("errorMessage", e.getMessage());
            MDC.put("errorClass", e.getClass().getSimpleName());

            log.error("API Request failed", e);

            throw e;

        } finally {
            // Clean up MDC
            MDC.clear();
        }
    }

    private boolean isSpringType(Object arg) {
        String className = arg.getClass().getName();
        return className.startsWith("org.springframework") ||
                className.startsWith("jakarta.servlet");
    }


}
