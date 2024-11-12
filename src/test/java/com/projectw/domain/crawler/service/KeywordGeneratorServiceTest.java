package com.projectw.domain.crawler.service;

import com.projectw.common.enums.ResponseCode;
import com.projectw.common.exceptions.NotFoundException;
import com.projectw.domain.crawler.service.KeywordGeneratorService;
import com.projectw.domain.store.entity.Store;
import com.projectw.domain.store.repository.StoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KeywordGeneratorServiceTest {

    @InjectMocks
    private KeywordGeneratorService keywordGeneratorService;

    @Mock
    private StoreRepository storeRepository;

    @Test
    @DisplayName("매장 정보로 키워드 생성 성공 테스트 - 구 포함 주소")
    void generateKeywords_WithGu_Success() {
        // given
        Long storeId = 1L;
        Store store = Store.builder()
                .title("테스트식당")
                .address("서울 강남구 역삼동 123-45")
                .build();

        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));

        // when
        List<String> keywords = keywordGeneratorService.generateKeywords(storeId);

        // then
        assertThat(keywords).hasSize(3)
                .containsExactly(
                        "테스트식당 강남",
                        "테스트식당 맛집",
                        "강남 맛집"
                );
    }

    @Test
    @DisplayName("매장 정보로 키워드 생성 성공 테스트 - 시 포함 주소")
    void generateKeywords_WithSi_Success() {
        // given
        Long storeId = 1L;
        Store store = Store.builder()
                .title("테스트식당")
                .address("경기 성남시 분당구 123-45")
                .build();

        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));

        // when
        List<String> keywords = keywordGeneratorService.generateKeywords(storeId);

        // then
        assertThat(keywords).hasSize(3)
                .containsExactly(
                        "테스트식당 성남",
                        "테스트식당 맛집",
                        "성남 맛집"
                );
    }

    @Test
    @DisplayName("존재하지 않는 매장 ID로 키워드 생성 시도시 예외 발생")
    void generateKeywords_WithInvalidStoreId_ThrowsException() {
        // given
        Long invalidStoreId = 999L;
        when(storeRepository.findById(invalidStoreId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> keywordGeneratorService.generateKeywords(invalidStoreId))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("해당 가게는 존재하지 않습니다.");
    }

    @Test
    @DisplayName("주소가 한 단어만 있는 경우 키워드 생성 테스트")
    void generateKeywords_WithSingleWordAddress_Success() {
        // given
        Long storeId = 1L;
        Store store = Store.builder()
                .title("테스트식당")
                .address("서울")
                .build();

        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));

        // when
        List<String> keywords = keywordGeneratorService.generateKeywords(storeId);

        // then
        assertThat(keywords).hasSize(3)
                .containsExactly(
                        "테스트식당 서울",
                        "테스트식당 맛집",
                        "서울 맛집"
                );
    }
}