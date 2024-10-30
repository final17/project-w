package com.projectw.domain.waiting.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class WaitingService {

    private final String REDIS_KEY = "store:waiting_rank";

    @Autowired
    private ZSetOperations<String, String> zSetOperations;

    // 레스토랑의 가중치 추가 또는 업데이트
    public void addOrUpdateStoreWeight(String storeId, double weight) {
        zSetOperations.add(REDIS_KEY, storeId, weight);
    }

    // 레스토랑의 가중치 증가/감소
    public void incrementWeight(String storeId, double delta) {
        zSetOperations.incrementScore(REDIS_KEY, storeId, delta);
    }

    // 상위 N개의 웨이팅 맛집 조회
    public Set<String> getTopRankedStores(int topN) {
        return zSetOperations.reverseRange(REDIS_KEY, 0, topN - 1);
    }
}
