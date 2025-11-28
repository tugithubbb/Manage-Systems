package com.example.demo.dto.request;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminUserDTO {
    private String id;
    private String username;
    private String fullname;
    private String email;
    private String phone;
    private String address;
    private String roleId;
    private String roleName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
