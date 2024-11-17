package com.projectw.domain.crawler.service;

import com.projectw.common.enums.ResponseCode;
import com.projectw.common.exceptions.NotFoundException;
import com.projectw.domain.store.entity.Store;
import com.projectw.domain.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class KeywordGeneratorService {

    private final StoreRepository storeRepository;

    /**
     * 매장 정보를 기반으로 검색 키워드 생성
     */
    public List<String> generateKeywords(Long storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new NotFoundException(ResponseCode.NOT_FOUND_STORE));

        List<String> keywords = new ArrayList<>();

        // 기본 키워드: 매장명 + 지역
        keywords.add(store.getTitle() + " " + extractRegion(store.getAddress()));

        // 매장명 + "맛집"
        keywords.add(store.getTitle() + " 맛집");

        // 지역 + "맛집"
        keywords.add(extractRegion(store.getAddress()) + " 맛집");

        return keywords;
    }

    /**
     * 주소에서 지역 추출 (예: "서울 강남구 역삼동" -> "강남")
     */
    private String extractRegion(String address) {
        String[] parts = address.split(" ");
        if (parts.length >= 2) {
            return parts[1].replace("구", "").replace("시", "");
        }
        return parts[0];
    }
}