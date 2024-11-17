package com.projectw.domain.waiting.controller;

import com.projectw.common.dto.SuccessResponse;
import com.projectw.domain.waiting.dto.StoreRank;
import com.projectw.domain.waiting.service.WaitingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/v2/waiting")
@RequiredArgsConstructor
public class WaitingController {

    private final WaitingService waitingService;

    /**
     * 가게의 웨이팅 가중치를 업데이트합니다.
     */
    @PostMapping("/stores/{storeId}/updateWeight")
    public ResponseEntity<SuccessResponse<Void>> updateWeight(
            @PathVariable String storeId,
            @RequestParam double weight) {

        if (storeId == null || storeId.isEmpty() || weight < 0) {
            throw new IllegalArgumentException("Invalid parameters for updating weight.");
        }

        waitingService.addOrUpdateStoreWeight(storeId, weight);
        return ResponseEntity.ok(SuccessResponse.of(null));
    }

    /**
     * 전체 상위 N개의 랭크가 높은 가게를 조회합니다.
     */
    @GetMapping("/topRanked")
    public ResponseEntity<SuccessResponse<StoreRank>> getTopRankedStores(@RequestParam int topN) {

        if (topN <= 0) {
            throw new IllegalArgumentException("Invalid topN parameter. It must be greater than 0.");
        }

        Set<StoreRank.StoreInfo> topStores = waitingService.getTopRankedStores(topN);
        StoreRank storeRankResponse = new StoreRank("웨이팅 맛집 순위입니다.", topStores);

        return ResponseEntity.ok(SuccessResponse.of(storeRankResponse));
    }
}