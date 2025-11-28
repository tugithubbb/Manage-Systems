package com.example.demo.dto.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FaceIDLoginRequest {
    private String username;
    private String faceImage; // Base64 string
}
