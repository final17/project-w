package com.projectw.security;

import com.projectw.common.enums.UserRole;
import com.projectw.domain.user.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;

@Getter
public class AuthUser {

    private final Long userId;
    private final String email;
    private final Collection<? extends GrantedAuthority> authorities;
    private final UserRole role;

    public AuthUser(Long userId, String email, UserRole role) {
        this.userId = userId;
        this.email = email;
        this.authorities = List.of(new SimpleGrantedAuthority(role.name()));
        this.role = role;
    }

    public User toUserEntity() {
        return new User(userId, email, role);
    }
}
