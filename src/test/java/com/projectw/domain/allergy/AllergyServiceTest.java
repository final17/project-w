package com.projectw.domain.allergy;

import com.projectw.domain.allergy.dto.response.AllergyResponseDto;
import com.projectw.domain.allergy.entity.Allergy;
import com.projectw.domain.allergy.repository.AllergyRepository;
import com.projectw.domain.allergy.service.AllergyService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AllergyServiceTest {

    @Mock
    private AllergyRepository allergyRepository;

    @InjectMocks
    private AllergyService allergyService;

    @Test
    void getAllAllergies_shouldReturnAllergies() {
        // Given
        Allergy allergy1 = new Allergy("Peanut", "Peanut allergy");
        Allergy allergy2 = new Allergy("Milk", "Milk allergy");
        List<Allergy> mockAllergies = List.of(allergy1, allergy2);

        when(allergyRepository.findAll()).thenReturn(mockAllergies);

        // When
        List<AllergyResponseDto.Basic> result = allergyService.getAllAllergies();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).name()).isEqualTo("Peanut");
        assertThat(result.get(1).name()).isEqualTo("Milk");

        verify(allergyRepository, times(1)).findAll();
    }

    @Test
    void getAllAllergies_shouldReturnEmptyListWhenNoAllergies() {
        // Given
        when(allergyRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        List<AllergyResponseDto.Basic> result = allergyService.getAllAllergies();

        // Then
        assertThat(result).isEmpty();

        verify(allergyRepository, times(1)).findAll();
    }
}