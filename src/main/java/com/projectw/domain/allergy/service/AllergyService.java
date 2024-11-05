package com.projectw.domain.allergy.service;

import com.projectw.domain.allergy.dto.response.AllergyResponseDto;
import com.projectw.domain.allergy.entity.Allergy;
import com.projectw.domain.allergy.repository.AllergyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AllergyService {

    private final AllergyRepository allergyRepository;

    @Cacheable(value = "allergies")
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