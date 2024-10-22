package com.projectw.domain.notification;

import com.projectw.common.dto.SuccessResponse;
import com.projectw.common.enums.UserRole;
import com.projectw.security.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
public class WaitingQueueController {

    private final WaitingQueueService storeQueueService;

    /**
     * 웨이팅 유저 정보 목록 조회
     */
    @GetMapping("/test/list")
    public ResponseEntity<SuccessResponse<WaitingQueueResponse.List>> list(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam long storeId ) {

        return ResponseEntity.ok(SuccessResponse.of(storeQueueService.getWaitingList(authUser, storeId)));
    }

    /**
     * SSE 연결
     */
    @GetMapping("/test/connect")
    public SseEmitter test1(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam long userId,
            @RequestParam long storeId) {
        // sse 연결
        return storeQueueService.connect(authUser, storeId);
    }

    /**
     * 대기열에 등록
     * @param authUser
     * @param userId
     * @param storeId
     */
    @GetMapping("/test/add")
    public void test2(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam long userId,
            @RequestParam long storeId) {
        storeQueueService.addUserToQueue(new AuthUser(userId, null, UserRole.ROLE_USER), storeId);
    }

    /**
     * 1등 유저 완료처리
     * @param user
     * @param userId
     * @param storeId
     */
    @GetMapping("/test/pop")
    public void testPop(
            @AuthenticationPrincipal AuthUser user,
            @RequestParam long userId,
            @RequestParam long storeId) {
        storeQueueService.popFirstUser(user, storeId);
    }

    /**
     * N번 뒤로 웨이팅 마감처리
     */
    @GetMapping("/test/cutline")
    public void testCutline(
            @AuthenticationPrincipal AuthUser user,
            @RequestParam long userId,
            @RequestParam long storeId, @RequestParam int cutline) {
        storeQueueService.clearQueueFromRank(new AuthUser(userId, null, UserRole.ROLE_USER), storeId, cutline);
    }

    /**
     * 유저가 웨이팅 취소
     */
    @GetMapping("/test/cancel")
    public void testCancel(
            @AuthenticationPrincipal AuthUser user,
            @RequestParam long userId,
            @RequestParam long storeId) {
        storeQueueService.cancel(user, storeId);
    }

    /**
     * 대기열 초기화
     */
    @GetMapping("/test/clear")
    public void test3(
            @AuthenticationPrincipal AuthUser user,
            @RequestParam long storeId,
            @RequestParam long userId) {
        storeQueueService.clearQueueFromRank(new AuthUser(userId, null, UserRole.ROLE_USER), storeId, 0);
    }
}
