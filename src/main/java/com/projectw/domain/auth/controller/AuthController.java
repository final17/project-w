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

import java.util.Set;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/auth/signup")
    public ResponseEntity<SuccessResponse<Signup>> signup(@Valid @RequestBody AuthRequest.Signup authRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.signup(authRequest));
    }

    @PostMapping("/auth/login")
    public ResponseEntity<SuccessResponse<AuthResponse.Login>> login(@Valid @RequestBody AuthRequest.Login authRequest) {
        return ResponseEntity.ok().body(authService.login(authRequest));
    }

    @PostMapping("/auth/logout")
    public ResponseEntity<SuccessResponse<Void>> logout(@AuthenticationPrincipal AuthUser user) {
        return ResponseEntity.ok(authService.logout(user));
    }

    @PostMapping("/auth/reissue")
    public ResponseEntity<?> reissue(@RequestHeader(JwtUtil.REFRESH_TOKEN_HEADER) String refreshToken) {
        return ResponseEntity.ok(authService.reissue(refreshToken));
    }

    @DeleteMapping("/auth/account")
    public ResponseEntity<SuccessResponse<SuccessResponse<Void>>> account(@AuthenticationPrincipal AuthUser user) {
        authService.deleteAccount(user);
        return ResponseEntity.ok(SuccessResponse.of(null));
    }

    @GetMapping("/auth/nickname/check")
    public ResponseEntity<SuccessResponse<AuthResponse.DuplicateCheck>> checkNickname(@RequestBody AuthRequest.CheckNickname request) {
        return ResponseEntity.ok(authService.checkNickname(request));
    }
    @GetMapping("/auth/email/check")
    public ResponseEntity<SuccessResponse<AuthResponse.DuplicateCheck>> checkEmail(@RequestBody AuthRequest.CheckEmail request) {
        return ResponseEntity.ok(authService.checkEmail(request));
    }
    @GetMapping("/auth/username/check")
    public ResponseEntity<SuccessResponse<AuthResponse.DuplicateCheck>> checkUsername(@RequestBody AuthRequest.CheckUsername request) {
        return ResponseEntity.ok(authService.checkUsername(request));
    }
    @PostMapping("/v2/auth/allergies")
    public ResponseEntity<SuccessResponse<Void>> updateUserAllergies(@AuthenticationPrincipal AuthUser user, @Valid @RequestBody Set<Long> allergyIds) {
        authService.updateUserAllergies(user.getUserId(), allergyIds);
        return ResponseEntity.ok(SuccessResponse.of(null));
    }
}
