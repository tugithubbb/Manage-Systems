package com.example.demo.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public class UserCreateRequest {

        @NotBlank(message = "FULLNAME_IS_REQUIRED")
        String fullname;

        @NotBlank(message = "USERNAME_IS_REQUIRED")
        @Size(min = 4, message = "USERNAME_INVALID")
        String username;

        @NotBlank(message = "PASSWORD_IS_REQUIRED")
        @Size(min = 6, message = "INVALID_PASSWORD")
        String password;

        @NotBlank(message = "EMAIL_IS_REQUIRED")
        @Email(message = "INVALID_EMAIL")
        String email;

        @NotBlank(message = "PHONE_IS_REQUIRED")
        String phone;
        @NotBlank(message = "ADDRESS_IS_REQUIRED")
        String address;
    }
