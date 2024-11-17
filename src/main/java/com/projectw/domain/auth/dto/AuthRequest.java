package com.projectw.domain.auth.dto;

import com.projectw.common.enums.UserRole;
import com.projectw.domain.auth.dto.AuthRequest.CheckEmail;
import com.projectw.domain.auth.dto.AuthRequest.CheckNickname;
import com.projectw.domain.auth.dto.AuthRequest.Login;
import com.projectw.domain.auth.dto.AuthRequest.Signup;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public sealed interface AuthRequest permits Signup, Login, CheckNickname, CheckEmail {
    record Login(
        @NotBlank String email,
        @NotBlank String password,
        String adminToken) implements AuthRequest {}

    record Signup (
        @Email String email,
        @NotBlank String password,
        @NotBlank String nickname,
        String adminToken,
        @NotNull UserRole userRole
    ) implements AuthRequest {
    }

    record CheckNickname(String nickname) implements AuthRequest {}
    record CheckEmail(String email) implements AuthRequest {}
}