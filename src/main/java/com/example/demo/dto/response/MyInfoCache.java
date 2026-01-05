package com.example.demo.dto.response;

import java.time.LocalDateTime;

public record MyInfoCache(
        String id,
        String username,
        String email,
        String fullName,
        String phoneNumber,
        LocalDateTime createdAt
) {
}
