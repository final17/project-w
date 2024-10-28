package com.projectw.domain.waiting.controller;

import com.projectw.common.dto.SuccessResponse;
import com.projectw.domain.waiting.dto.WaitingQueueResponse;
import com.projectw.domain.waiting.service.WaitingQueueService;
import com.projectw.security.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class WaitingOwnerController {

    private final WaitingQueueService storeQueueService;

    /**
     * 웨이팅 유저 정보 목록 조회
     */
    @GetMapping("/api/v2/owner/stores/{storeId}/waitings/list")
    public ResponseEntity<SuccessResponse<WaitingQueueResponse.List>> list(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable long storeId ) {

        return ResponseEntity.ok(SuccessResponse.of(storeQueueService.getWaitingList(authUser, storeId)));
    }

    /**
     * 1등 유저 완료처리
     */
    @PostMapping("/api/v2/owner/stores/{storeId}/waitings/poll")
    public ResponseEntity<SuccessResponse<Void>> pollUser(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable long storeId) {

        storeQueueService.pollFirstUser(authUser, storeId);
        return ResponseEntity.ok().body(SuccessResponse.of(null));
    }

    /**
     * N번 뒤로 웨이팅 마감처리
     */
    @DeleteMapping("/api/v2/owner/stores/{storeId}/waitings/clear")
    public ResponseEntity<SuccessResponse<Void>> waitingCut(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable long storeId,
            @RequestParam(defaultValue = "0") int cutline) {

        storeQueueService.clearQueueFromRank(authUser, storeId, cutline);
        return ResponseEntity.ok().body(SuccessResponse.of(null));
    }
}
