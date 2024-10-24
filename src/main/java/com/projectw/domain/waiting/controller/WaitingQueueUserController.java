package com.projectw.domain.waiting.controller;

import com.projectw.common.dto.SuccessResponse;
import com.projectw.domain.waiting.dto.WaitingQueueResponse;
import com.projectw.domain.waiting.service.WaitingQueueService;
import com.projectw.security.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
public class WaitingQueueUserController {

    private final WaitingQueueService waitingQueueService;

    /**
     * SSE 연결
     */
    @GetMapping("/api/v2/user/stores/{storeId}/waitings/connection")
    public ResponseEntity<SseEmitter> test1(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable long storeId) {

        return ResponseEntity.ok(waitingQueueService.connect(authUser, storeId));
    }

    /**
     * 대기열에 등록
     */
    @PostMapping("/api/v2/user/stores/{storeId}/waitings")
    public ResponseEntity<SuccessResponse<WaitingQueueResponse.Info>> test2(
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


}
