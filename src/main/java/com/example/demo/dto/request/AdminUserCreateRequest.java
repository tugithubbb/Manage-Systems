package com.example.demo.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserCreateRequest {
    private String username;
    private String fullname;
    private String email;
    private String password;
    private String phone;
    private String address;
    private String roleId;
}
