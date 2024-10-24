package com.projectw.domain.auth.dto;

import com.projectw.common.enums.UserRole;
import com.projectw.domain.auth.dto.AuthRequest.CheckEmail;
import com.projectw.domain.auth.dto.AuthRequest.CheckNickname;
import com.projectw.domain.auth.dto.AuthRequest.CheckUsername;
import com.projectw.domain.auth.dto.AuthRequest.Login;
import com.projectw.domain.auth.dto.AuthRequest.Signup;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public sealed interface AuthRequest permits Signup, Login, CheckNickname, CheckEmail,
    CheckUsername {
    record Login(
        @NotBlank String username,
        @NotBlank String password,
        String adminToken) implements AuthRequest {}

    record Signup (
        @NotBlank String username,
        @NotBlank String password,
        @Email String email,
        @NotBlank String nickname,
        String adminToken,
        @NotNull UserRole userRole,
        List<Long> allergyIds
    ) implements AuthRequest {
    }

    record CheckNickname(String nickname) implements AuthRequest {}
    record CheckEmail(String email) implements AuthRequest {}
    record CheckUsername(String username) implements AuthRequest {}
}