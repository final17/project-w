package com.projectw.domain.allergy.controller;

import com.projectw.common.dto.SuccessResponse;
import com.projectw.domain.allergy.dto.request.AllergyUpdateRequestDto;
import com.projectw.domain.allergy.dto.response.AllergyResponseDto;
import com.projectw.domain.allergy.service.AllergyService;
import com.projectw.domain.auth.service.AuthService;
import com.projectw.security.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v2")
@RequiredArgsConstructor
public class AllergyController {

    private final AllergyService allergyService;
    private final AuthService authService;

    @PostMapping("/user/allergies")
    public ResponseEntity<SuccessResponse<Void>> updateUserAllergies(@AuthenticationPrincipal AuthUser authUser, @RequestBody AllergyUpdateRequestDto allergyUpdateRequest) {
        Long userId = authUser.getUserId();
        authService.updateUserAllergies(userId, allergyUpdateRequest.getAllergyIds());
        return ResponseEntity.ok(SuccessResponse.empty());
    }

    @GetMapping("/allergies")
    public ResponseEntity<List<AllergyResponseDto>> getAllAllergies() {
        List<AllergyResponseDto> allergies = allergyService.getAllAllergies();
        return ResponseEntity.ok(allergies);
    }
}