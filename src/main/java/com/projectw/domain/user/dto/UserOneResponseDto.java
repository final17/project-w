package com.projectw.domain.user.dto;

import com.projectw.common.enums.UserRole;
import com.projectw.domain.user.entity.User;
import lombok.Getter;

@Getter
public class UserOneResponseDto {
    private Long id;
    private String username;
    private String email;
    private UserRole userRole;

    public UserOneResponseDto(User user) {
        id = user.getId();
        username =  user.getUsername();
        email = user.getEmail();
        userRole = user.getRole();
    }
}
