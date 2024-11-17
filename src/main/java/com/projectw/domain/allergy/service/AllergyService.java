package com.projectw.domain.allergy.service;

import com.projectw.domain.allergy.dto.response.AllergyResponseDto;
import com.projectw.domain.allergy.entity.Allergy;
import com.projectw.domain.allergy.repository.AllergyRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AllergyService {

    private static final Logger logger = LoggerFactory.getLogger(AllergyService.class);

    private final AllergyRepository allergyRepository;

    @Cacheable(value = "allergies")
    public List<AllergyResponseDto.Basic> getAllAllergies() {

        List<Allergy> allergies = allergyRepository.findAll();

        if (allergies.isEmpty()) {
            logger.warn("알레르기 정보를 찾을 수 없습니다.");
            return Collections.emptyList();
        }

        return allergies.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private AllergyResponseDto.Basic convertToDto(Allergy allergy) {
        return new AllergyResponseDto.Basic(
                allergy.getId(),
                allergy.getName(),
                allergy.getDescription()
        );
    }
}