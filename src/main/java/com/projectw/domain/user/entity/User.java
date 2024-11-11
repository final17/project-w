package com.projectw.domain.user.entity;

import com.projectw.common.entity.Timestamped;
import com.projectw.common.enums.UserRole;
import com.projectw.security.AuthUser;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends Timestamped {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, unique = true)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Column(nullable = false, name = "is_deleted")
    private boolean isDeleted = false;

    @Builder
    public User(String password, String email, String nickname, UserRole role) {
        this.password = password;
        this.email = email;
        this.nickname = nickname;
        this.role = role;
    }

    public User(Long userId, String email, UserRole role) {
        this.id = userId;
        this.email = email;
        this.role = role;
    }

    public static User fromAuthUser(AuthUser authUser) {
        return new User(authUser.getUserId(), authUser.getEmail(), authUser.getRole());
    }

    public void delete() {
        isDeleted = true;
    }
}
