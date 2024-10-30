package com.projectw.domain.waiting.controller;

import com.projectw.common.config.RedisWeightingSystem;
import com.projectw.domain.waiting.dto.StoreRankResponse;
import com.projectw.domain.waiting.service.WaitingService;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/v2/waiting")
public class WaitingController {

    private final RedisWeightingSystem redisWeightingSystem;
    private final WaitingService waitingService;

    public WaitingController(RedisWeightingSystem redisWeightingSystem, WaitingService waitingService) {
        this.redisWeightingSystem = redisWeightingSystem;
        this.waitingService = waitingService;
    }

    @PostMapping("/updateWeight")
    public StoreRankResponse updateWeight(@RequestParam String storeId,
                                          @RequestParam int reservations,
                                          @RequestParam int waitingCount) {
        if (storeId == null || storeId.isEmpty() || reservations < 0 || waitingCount < 0) {
            throw new IllegalArgumentException("Invalid parameters for updating weight.");
        }

        redisWeightingSystem.updateStoreWeight(storeId, reservations, waitingCount);
        return new StoreRankResponse("Weight updated successfully!");
    }

    @GetMapping("/topRanked")
    public StoreRankResponse getTopRankedStores(@RequestParam int topN) {
        Set<String> topStores = waitingService.getTopRankedStores(topN);
        return new StoreRankResponse("Top ranked stores retrieved successfully!", topStores);
    }
}