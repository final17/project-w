package com.projectw.domain.waiting.controller;

import com.projectw.common.dto.SuccessResponse;
import com.projectw.domain.waiting.dto.WaitingQueueResponse;
import com.projectw.domain.waiting.service.WaitingHistoryService;
import com.projectw.domain.waiting.service.WaitingQueueService;
import com.projectw.security.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.awt.print.Pageable;

@RestController
@RequiredArgsConstructor
public class WaitingQueueUserController {

    private final WaitingQueueService waitingQueueService;
    private final WaitingHistoryService waitingHistoryService;

    /**
     * 알림을 받기 위해 서버에게 SSE 요청
     */
    @GetMapping("/api/v2/user/stores/{storeId}/waitings/connection")
    public ResponseEntity<SseEmitter> connectToServer(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable long storeId) {

        return ResponseEntity.ok(waitingQueueService.connect(authUser, storeId));
    }

    /**
     * 대기열에 등록
     */
    @PostMapping("/api/v2/user/stores/{storeId}/waitings")
    public ResponseEntity<SuccessResponse<WaitingQueueResponse.Info>> addToWaitingQueue(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable long storeId) {
        return ResponseEntity.ok(SuccessResponse.of(waitingQueueService.addUserToQueue(authUser, storeId)));
    }

    /**
     * 유저가 웨이팅 취소
     */
    @DeleteMapping("/api/v2/user/stores/{storeId}/waitings")
    public ResponseEntity<SuccessResponse<Void>> waitingCancel(
            @AuthenticationPrincipal AuthUser user,
            @PathVariable long storeId) {

        waitingQueueService.cancel(user, storeId);
        return ResponseEntity.ok().body(SuccessResponse.of(null));
    }

    /**
     * 내가 웨이팅 등록한 가게 아이디 목록 조회
     * @param authUser
     * @return
     */
    @GetMapping("/api/v2/user/waitings/stores")
    public ResponseEntity<SuccessResponse<WaitingQueueResponse.MyWaitingStoreList>> getRegisteredWaitingStoreIds(@AuthenticationPrincipal AuthUser authUser) {
        return ResponseEntity.ok(SuccessResponse.of(waitingHistoryService.getRegisteredStoreList(authUser)));
    }

    /**
     * 웨이팅 대기열에 등록되어 있는지 확인
     */
    @GetMapping("/api/v2/user/stores/{storeId}/waitings")
    public ResponseEntity<SuccessResponse<WaitingQueueResponse.WaitingInfo>> checkWaitingStatus(
        @AuthenticationPrincipal AuthUser authUser,
        @PathVariable long storeId
    ) {
        return ResponseEntity.ok(SuccessResponse.of(waitingQueueService.checkWaitingStatus(authUser, storeId)));
    }



}
