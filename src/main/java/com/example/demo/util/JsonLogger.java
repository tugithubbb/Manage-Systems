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
        withMDC(() -> {
            setCommonFields("REQUEST");
            MDC.put("http_method", method);
            MDC.put("url", url);
            if (requestData != null) {
                MDC.put("request_data", toJson(requestData));
            }
            log.info("API Request received");
        });
    }

    // ===== RESPONSE =====
    public static void logResponse(Object responseData, int httpStatus, long durationMs) {
        withMDC(() -> {
            setCommonFields("RESPONSE");
            MDC.put("http_status", String.valueOf(httpStatus));
            MDC.put("duration", durationMs + "ms");
            if (responseData != null) {
                MDC.put("response_data", toJson(responseData));
            }
            log.info("API Response sent");
        });
    }

    // ===== INFO =====
    public static void info(String message) {
        withMDC(() -> {
            setCommonFields("API");
            log.info(message);
        });
    }

    public static void info(String message, Object data) {
        withMDC(() -> {
            setCommonFields("API");
            if (data != null) {
                MDC.put("data", toJson(data));
            }
            log.info(message);
        });
    }

    // ===== DEBUG =====
    public static void debug(String message) {
        withMDC(() -> {
            setCommonFields("API");
            log.debug(message);
        });
    }

    public static void debug(String message, Object data) {
        withMDC(() -> {
            setCommonFields("API");
            if (data != null) {
                MDC.put("data", toJson(data));
            }
            log.debug(message);
        });
    }

    // ===== WARN =====
    public static void warn(String message) {
        withMDC(() -> {
            setCommonFields("API");
            log.warn(message);
        });
    }

    public static void warn(String message, Object data) {
        withMDC(() -> {
            setCommonFields("API");
            if (data != null) {
                MDC.put("data", toJson(data));
            }
            log.warn(message);
        });
    }

    // ===== ERROR =====
    public static void error(String message) {
        withMDC(() -> {
            setCommonFields("API");
            log.error(message);
        });
    }

    public static void error(String message, Object data) {
        withMDC(() -> {
            setCommonFields("API");
            if (data != null) {
                MDC.put("data", toJson(data));
            }
            log.error(message);
        });
    }

    public static void error(String message, Throwable throwable) {
        withMDC(() -> {
            setCommonFields("API");
            MDC.put("error_message", throwable.getMessage());
            MDC.put("error_class", throwable.getClass().getSimpleName());
            log.error(message, throwable);
        });
    }

    public static void error(String message, Object data, Throwable throwable) {
        withMDC(() -> {
            setCommonFields("API");
            if (data != null) {
                MDC.put("data", toJson(data));
            }
            MDC.put("error_message", throwable.getMessage());
            MDC.put("error_class", throwable.getClass().getSimpleName());
            log.error(message, throwable);
        });
    }

    /* =====================================================
     * MDC MANAGEMENT (CORE)
     * ===================================================== */

    /**
     * Mỗi log = MDC sandbox riêng
     * Giữ traceId/spanId do Filter/Micrometer tạo
     */
    private static void withMDC(Runnable action) {
        Map<String, String> previous = MDC.getCopyOfContextMap();
        try {
            action.run();
        } finally {
            restoreMDC(previous);
        }
    }

    private static void restoreMDC(Map<String, String> previous) {
        if (previous != null) {
            MDC.setContextMap(previous);
        } else {
            MDC.clear();
        }
    }

    /* =====================================================
     * COMMON FIELDS
     * ===================================================== */

    private static void setCommonFields(String type) {
        StackTraceElement caller = getCallerInfo();
        MDC.put("type", type);
        MDC.put("class_name", caller.getClassName());
        MDC.put("method_name", caller.getMethodName());
        MDC.put("line_code", String.valueOf(caller.getLineNumber()));
    }

    private static StackTraceElement getCallerInfo() {
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        // [0] getStackTrace
        // [1] getCallerInfo
        // [2] setCommonFields
        // [3] withMDC lambda
        // [4] public log method
        // [5] real caller
        return stack.length > 5 ? stack[5] : stack[stack.length - 1];
    }

    /* =====================================================
     * JSON
     * ===================================================== */

    private static String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return String.valueOf(obj);
        }
    }

}
