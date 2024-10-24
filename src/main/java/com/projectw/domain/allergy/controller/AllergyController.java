package com.projectw.domain.allergy.controller;

import com.projectw.domain.allergy.dto.response.AllergyResponseDto;
import com.projectw.domain.allergy.service.AllergyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v2/allergies")
@RequiredArgsConstructor
public class AllergyController {

    private final AllergyService allergyService;

    @GetMapping
    public ResponseEntity<List<AllergyResponseDto>> getAllAllergies() {
        List<AllergyResponseDto> allergies = allergyService.getAllAllergies();
        return ResponseEntity.ok(allergies);
    }
}