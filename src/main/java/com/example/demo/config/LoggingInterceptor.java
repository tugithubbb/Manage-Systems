package com.example.demo.config;

import com.example.demo.util.JsonLogger;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class LoggingInterceptor implements HandlerInterceptor {
    private final ObjectMapper objectMapper;
    private static final String START_TIME = "startTime";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        long startTime = System.currentTimeMillis();
        request.setAttribute(START_TIME, startTime);

        // Log REQUEST
        String method = request.getMethod();
        String url = request.getRequestURI();

        Map<String, Object> requestData = new HashMap<>();
        requestData.put("method", method);
        requestData.put("url", url);
        requestData.put("query_params", getQueryParams(request));

        // Get request body if exists
        if (request instanceof ContentCachingRequestWrapper) {
            ContentCachingRequestWrapper wrapper = (ContentCachingRequestWrapper) request;
            byte[] content = wrapper.getContentAsByteArray();
            if (content.length > 0) {
                String body = new String(content, StandardCharsets.UTF_8);
                try {
                    requestData.put("body", objectMapper.readValue(body, Object.class));
                } catch (Exception e) {
                    requestData.put("body", body);
                }
            }
        }

        JsonLogger.logRequest(method, url, requestData);

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        long startTime = (Long) request.getAttribute(START_TIME);
        long duration = System.currentTimeMillis() - startTime;
        int httpStatus = response.getStatus();

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("http_status", httpStatus);

        // Get response body if exists
        if (response instanceof ContentCachingResponseWrapper) {
            ContentCachingResponseWrapper wrapper = (ContentCachingResponseWrapper) response;
            byte[] content = wrapper.getContentAsByteArray();
            if (content.length > 0) {
                String body = new String(content, StandardCharsets.UTF_8);
                try {
                    responseData.put("body", objectMapper.readValue(body, Object.class));
                } catch (Exception e) {
                    responseData.put("body", body);
                }
            }
        }

        // Log RESPONSE
        JsonLogger.logResponse(responseData, httpStatus, duration);

        // Log ERROR if exception occurred
        if (ex != null) {
            JsonLogger.error("Request processing failed", ex);
        }
    }

    private Map<String, String> getQueryParams(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        request.getParameterMap().forEach((key, values) -> {
            if (values.length > 0) {
                params.put(key, values[0]);
            }
        });
        return params;
    }


}
