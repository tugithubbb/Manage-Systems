package com.example.demo.exception;


import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import lombok.Getter;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(1001, "Uncategorized error", HttpStatus.BAD_REQUEST),
    USER_EXISTED(1002, "User existed", HttpStatus.BAD_REQUEST),
    USERNAME_INVALID(1003, "Username must be at least {min} characters", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD(1004, "Password must be at least {min} characters", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(1005, "User not existed", HttpStatus.NOT_FOUND),
    USER_NOT_FOUND(10010, "User not existed", HttpStatus.NOT_FOUND),
        UNAUTHENTICATED(1006, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1007, "You do not have permission", HttpStatus.FORBIDDEN),
    INVALID_DOB(1008, "Your age must be at least {min}", HttpStatus.BAD_REQUEST),
    INVALID_TOKEN(1015, "Your age must be at least {min}", HttpStatus.UNAUTHORIZED),
    LoginLai(1015, "loginlai", HttpStatus.UNAUTHORIZED),
    BAD_REQUEST(1017, "Your age must be at least {min}", HttpStatus.BAD_REQUEST),
    ACCESS_TOKEN_EXPIRED(1002, "Access token has expired", HttpStatus.UNAUTHORIZED),
    FACE_ID_NOT_REGISTERED(10023, "Access token has expired", HttpStatus.UNAUTHORIZED),
    FACE_ID_VERIFICATION_FAILED(10023, "Access token has expired", HttpStatus.UNAUTHORIZED),
    FORBIDDEN(10023, "Access token has expired", HttpStatus.UNAUTHORIZED)
    ;

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;
}

