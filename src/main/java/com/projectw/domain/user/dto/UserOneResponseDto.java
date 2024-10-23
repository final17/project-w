package com.projectw.domain.user.dto;

import com.projectw.common.enums.UserRole;
import com.projectw.domain.user.entity.User;
import lombok.Getter;

@Getter
public class UserOneResponseDto {
    private Long id;
    private String email;
    private UserRole userRole;

    public UserOneResponseDto(User user) {
        id = user.getId();
        email = user.getEmail();
        userRole = user.getRole();
    }
}
