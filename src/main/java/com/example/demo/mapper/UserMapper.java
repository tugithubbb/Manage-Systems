package com.example.demo.mapper;

import com.example.demo.Entity.AdminUser;
import com.example.demo.dto.request.UserCreateRequest;
import com.example.demo.dto.response.MyInfoResponse;
import com.example.demo.dto.response.UserInfoResponse;
import com.example.demo.dto.response.UserResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    AdminUser toUser (UserCreateRequest userCreateRequest);
    UserResponse toUserResponse(AdminUser adminUser);
    UserInfoResponse toUserInfoResponse(AdminUser adminUser);
    MyInfoResponse toMyInfoResponse(AdminUser adminUser);
}
