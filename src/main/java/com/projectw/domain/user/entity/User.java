package com.projectw.domain.user.entity;

import com.projectw.common.entity.Timestamped;
import com.projectw.common.enums.UserRole;
import com.projectw.domain.allergy.entity.Allergy;
import com.projectw.security.AuthUser;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends Timestamped {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, unique = true)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @ManyToMany
    @JoinTable(
            name = "user_allergies", // 유저-알레르기 중간 테이블
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "allergy_id")
    )
    private Set<Allergy> allergies = new HashSet<>();

    @Builder
    public User(String username, String password, String email, String nickname, UserRole role) {
        this.username = username;
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

    // 유저 알레르기 업데이트
    public void updateAllergies(Set<Allergy> allergies) {
        this.allergies = allergies;
    }
}
