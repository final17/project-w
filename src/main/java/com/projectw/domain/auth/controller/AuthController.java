package com.projectw.domain.auth.controller;

import com.projectw.common.dto.SuccessResponse;
import com.projectw.domain.auth.dto.AuthRequest;
import com.projectw.domain.auth.dto.AuthResponse;
import com.projectw.domain.auth.dto.AuthResponse.Signup;
import com.projectw.domain.auth.service.AuthService;
import com.projectw.security.AuthUser;
import com.projectw.security.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<SuccessResponse<Signup>> signup(@Valid @RequestBody AuthRequest.Signup authRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.signup(authRequest));
    }

    @PostMapping("/login")
    public ResponseEntity<SuccessResponse<AuthResponse.Login>> login(@Valid @RequestBody AuthRequest.Login authRequest) {
        AuthResponse.Login result = authService.login(authRequest);
        return ResponseEntity.ok().header(JwtUtil.AUTHORIZATION_HEADER, result.accessToken()).body(SuccessResponse.of(result));
    }

    @PostMapping("/logout")
    public ResponseEntity<SuccessResponse<Void>> logout(@AuthenticationPrincipal AuthUser user) {
        return ResponseEntity.ok(authService.logout(user));
    }

    @PostMapping("/reissue")
    public ResponseEntity<?> reissue(@RequestHeader(JwtUtil.REFRESH_TOKEN_HEADER) String refreshToken) {
        AuthResponse.Reissue result = authService.reissue(refreshToken);
        return ResponseEntity.ok().header(JwtUtil.AUTHORIZATION_HEADER, result.accessToken()).body(SuccessResponse.of(result));
    }

    @DeleteMapping("/account")
    public ResponseEntity<SuccessResponse<SuccessResponse<Void>>> account(@AuthenticationPrincipal AuthUser user) {
        authService.deleteAccount(user);
        return ResponseEntity.ok(SuccessResponse.of(null));
    }

    @GetMapping("/nickname/check")
    public ResponseEntity<SuccessResponse<AuthResponse.DuplicateCheck>> checkNickname(@RequestBody AuthRequest.CheckNickname request) {
        return ResponseEntity.ok(authService.checkNickname(request));
    }

    @GetMapping("/email/check")
    public ResponseEntity<SuccessResponse<AuthResponse.DuplicateCheck>> checkEmail(@RequestBody AuthRequest.CheckEmail request) {
        return ResponseEntity.ok(authService.checkEmail(request));
    }
}