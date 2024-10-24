package com.projectw.domain.allergy.service;

import com.projectw.domain.allergy.dto.response.AllergyResponseDto;
import com.projectw.domain.allergy.entity.Allergy;
import com.projectw.domain.allergy.repository.AllergyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AllergyService {

    private final AllergyRepository allergyRepository;

    // 모든 알레르기 정보 조회 (DTO로 변환)
    public List<AllergyResponseDto> getAllAllergies() {
        List<Allergy> allergies = allergyRepository.findAll();

        return allergies.stream()
                .map(allergy -> new AllergyResponseDto(
                        allergy.getId(),
                        allergy.getName(),
                        allergy.getDescription())) // 필요한 필드를 DTO로 변환
                .collect(Collectors.toList());
    }
}