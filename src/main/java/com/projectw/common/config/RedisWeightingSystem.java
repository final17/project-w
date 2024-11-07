package com.projectw.common.config;

import com.projectw.domain.waiting.service.WaitingService;
import org.springframework.stereotype.Component;

@Component
public class RedisWeightingSystem {
    private final WaitingService waitingService;

    public RedisWeightingSystem(WaitingService waitingService) {
        this.waitingService = waitingService;
    }

    // 가중치를 계산하여 업데이트하는 메서드
    public void updateStoreWeight(String storeId, String storeName, int reservations, int waitingCount) {
        double weight = calculateWeight(reservations, waitingCount);
        waitingService.addOrUpdateStoreWeight(storeId, weight);
    }

    // 간단한 가중치 계산 로직 예시
    private double calculateWeight(int reservations, int waitingCount) {
        return reservations * 1.5 + waitingCount;
    }
}