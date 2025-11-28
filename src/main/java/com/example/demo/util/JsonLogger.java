package com.example.demo.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class JsonLogger {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    // ===== REQUEST =====
    public static void logRequest(String method, String url, Object requestData) {
        setCommonFields("REQUEST", "info");
        MDC.put("method", method);
        MDC.put("url", url);
        if (requestData != null) {
            MDC.put("request_data", toJson(requestData));
        }
        log.info("API Request received");
        clearMDC();
    }

    // ===== RESPONSE =====
    public static void logResponse(Object responseData, int httpStatus, long duration) {
        setCommonFields("RESPONSE", "info");
        MDC.put("http_status", String.valueOf(httpStatus));
        MDC.put("duration", duration + "ms");
        if (responseData != null) {
            MDC.put("response_data", toJson(responseData));
        }
        log.info("API Response sent");
        clearMDC();
    }

    // ===== INFO =====
    public static void info(String message) {
        setCommonFields("API", "info");
        log.info(message);
        clearMDC();
    }

    public static void info(String message, Object data) {
        setCommonFields("API", "info");
        if (data != null) {
            MDC.put("data", toJson(data));
        }
        log.info(message);
        clearMDC();
    }

    // ===== DEBUG =====
    public static void debug(String message) {
        setCommonFields("API", "debug");
        log.debug(message);
        clearMDC();
    }

    public static void debug(String message, Object data) {
        setCommonFields("API", "debug");
        if (data != null) {
            MDC.put("data", toJson(data));
        }
        log.debug(message);
        clearMDC();
    }

    // ===== WARN =====
    public static void warn(String message) {
        setCommonFields("API", "warn");
        log.warn(message);
        clearMDC();
    }

    public static void warn(String message, Object data) {
        setCommonFields("API", "warn");
        if (data != null) {
            MDC.put("data", toJson(data));
        }
        log.warn(message);
        clearMDC();
    }

    // ===== ERROR =====
    public static void error(String message) {
        setCommonFields("API", "error");
        log.error(message);
        clearMDC();
    }

    public static void error(String message, Object data) {
        setCommonFields("API", "error");
        if (data != null) {
            MDC.put("data", toJson(data));
        }
        log.error(message);
        clearMDC();
    }

    public static void error(String message, Throwable throwable) {
        setCommonFields("API", "error");
        MDC.put("error_message", throwable.getMessage());
        MDC.put("error_class", throwable.getClass().getSimpleName());
        log.error(message, throwable);
        clearMDC();
    }

    public static void error(String message, Object data, Throwable throwable) {
        setCommonFields("API", "error");
        if (data != null) {
            MDC.put("data", toJson(data));
        }
        MDC.put("error_message", throwable.getMessage());
        MDC.put("error_class", throwable.getClass().getSimpleName());
        log.error(message, throwable);
        clearMDC();
    }

    // ===== HELPER METHODS =====
    private static void setCommonFields(String type, String level) {
        StackTraceElement caller = getCallerInfo();
        MDC.put("type", type);
        MDC.put("level", level);
        MDC.put("line_code", String.valueOf(caller.getLineNumber()));
        MDC.put("class_name", caller.getClassName());
        MDC.put("method_name", caller.getMethodName());
    }

    private static StackTraceElement getCallerInfo() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        // Skip: getStackTrace(0), getCallerInfo(1), setCommonFields(2), public method(3), actual caller(4)
        return stackTrace.length > 4 ? stackTrace[4] : stackTrace[stackTrace.length - 1];
    }

    private static String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return obj.toString();
        }
    }

    private static void clearMDC() {
        MDC.clear();
    }
}
