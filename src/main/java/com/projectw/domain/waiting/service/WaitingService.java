package com.projectw.domain.waiting.service;

import com.projectw.domain.store.entity.Store;
import com.projectw.domain.store.repository.StoreRepository;
import com.projectw.domain.waiting.dto.StoreRank;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WaitingService {

    private final String REDIS_KEY = "store:waiting_rank";

    private final ZSetOperations<String, String> zSetOperations;
    private final StoreRepository storeRepository;

    // 레스토랑의 가중치 추가 또는 업데이트
    public void addOrUpdateStoreWeight(String storeId, double weight) {
        zSetOperations.add(REDIS_KEY, storeId, weight);
    }

    // 레스토랑의 가중치 증가/감소
    public void incrementWeight(String storeId, double delta) {
        zSetOperations.incrementScore(REDIS_KEY, storeId, delta);
    }

    // 전체 상위 N개의 웨이팅 맛집 조회
    public Set<StoreRank.StoreInfo> getTopRankedStores(int topN) {
        // Redis에서 상위 N개의 storeId를 조회
        Set<String> topStoreIds = zSetOperations.reverseRange(REDIS_KEY, 0, topN - 1);
        if (topStoreIds == null) return new LinkedHashSet<>();

        // 각 storeId로 StoreRepository에서 가게 제목을 조회하고 StoreInfo 객체 생성
        return topStoreIds.stream()
                .map(storeId -> {
                    // storeId로 가게 정보 조회
                    Store store = storeRepository.findById(Long.parseLong(storeId))
                            .orElse(null);
                    String storeTitle = (store != null) ? store.getTitle() : "가게를 찾을 수 없습니다";
                    return new StoreRank.StoreInfo(storeId, storeTitle);
                })
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}