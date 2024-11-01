package com.projectw.domain.like.controller;

import com.projectw.common.dto.SuccessResponse;
import com.projectw.domain.like.dto.Response.LikeResponseDto;
import com.projectw.domain.like.service.LikeService;
import com.projectw.security.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v2")
@RequiredArgsConstructor
public class LikeController {
    private final LikeService likeService;

    @PatchMapping("/reviews/{reviewId}/likes")
    public ResponseEntity<Map<String, Object>> toggleLike(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal AuthUser user
    ) {
        boolean isLiked = likeService.toggleReviewLike(reviewId, user.getEmail());
        long likeCount = likeService.getLikeCount(reviewId);

        Map<String, Object> response = new HashMap<>();
        response.put("liked", isLiked);
        response.put("likeCount", likeCount);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/reviews/{reviewId}/likes")
    public ResponseEntity<Map<String, Object>> getLikeStatus(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal AuthUser user
    ) {
        boolean hasLiked = likeService.hasLikedReview(reviewId, user.getEmail());
        long likeCount = likeService.getLikeCount(reviewId);

        Map<String, Object> response = new HashMap<>();
        response.put("liked", hasLiked);
        response.put("likeCount", likeCount);

        return ResponseEntity.ok(response);
    }

    // 메뉴에 대한 좋아요 상태 및 좋아요 수 조회
    @GetMapping("/stores/{storeId}/menus/{menuId}/like-status")
    public ResponseEntity<SuccessResponse<LikeResponseDto>> getMenuLikeStatus(
            @PathVariable Long storeId,
            @PathVariable Long menuId,
            @AuthenticationPrincipal AuthUser authUser) {

        LikeResponseDto response = likeService.getMenuLikeStatus(storeId, menuId, authUser.getUserId());
        return ResponseEntity.ok(SuccessResponse.of(response));
    }

    @PostMapping("/stores/{storeId}/menus/{menuId}/like")
    public ResponseEntity<SuccessResponse<Boolean>> toggleLikeMenu(
            @PathVariable Long storeId,
            @PathVariable Long menuId,
            @AuthenticationPrincipal AuthUser authUser) {

        boolean isLiked = likeService.toggleMenuLike(storeId, menuId, authUser.getUserId());
        return ResponseEntity.ok(SuccessResponse.of(isLiked));
    }

    @GetMapping("/stores/{storeId}/menus/{menuId}/likes")
    public ResponseEntity<SuccessResponse<Long>> getMenuLikeCount(
            @PathVariable Long storeId,
            @PathVariable Long menuId) {

        long likeCount = likeService.getLikeCountForMenu(storeId, menuId);
        return ResponseEntity.ok(SuccessResponse.of(likeCount));
    }

    @GetMapping("/stores/{storeId}/menus/{menuId}/has-liked")
    public ResponseEntity<SuccessResponse<Boolean>> hasLikedMenu(
            @PathVariable Long storeId,
            @PathVariable Long menuId,
            @AuthenticationPrincipal AuthUser authUser) {

        boolean hasLiked = likeService.hasLikedMenu(storeId, menuId, authUser.getUserId());
        return ResponseEntity.ok(SuccessResponse.of(hasLiked));
    }
}
