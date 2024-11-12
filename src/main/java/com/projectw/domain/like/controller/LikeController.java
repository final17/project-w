package com.projectw.domain.like.controller;

import com.projectw.common.dto.SuccessResponse;
import com.projectw.domain.like.dto.LikeResponseDto;
import com.projectw.domain.like.service.LikeService;
import com.projectw.security.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v2")
@RequiredArgsConstructor
public class LikeController {
    private final LikeService likeService;

    @PatchMapping("/reviews/{reviewId}/likes")
    public ResponseEntity<LikeResponseDto.Basic> toggleReviewLike(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal AuthUser user) {
        boolean isLiked = likeService.toggleReviewLike(reviewId, user.getEmail());
        long likeCount = likeService.getLikeCount(reviewId);
        return ResponseEntity.ok(new LikeResponseDto.Basic(isLiked, likeCount));
    }

    @GetMapping("/reviews/{reviewId}/likes")
    public ResponseEntity<LikeResponseDto.Basic> getReviewLikeStatus(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal AuthUser user) {
        boolean hasLiked = likeService.hasLikedReview(reviewId, user.getEmail());
        long likeCount = likeService.getLikeCount(reviewId);
        return ResponseEntity.ok(new LikeResponseDto.Basic(hasLiked, likeCount));
    }

    @PostMapping("/stores/{storeId}/menus/{menuId}/like")
    public ResponseEntity<SuccessResponse<Boolean>> toggleMenuLike(
            @PathVariable Long storeId,
            @PathVariable Long menuId,
            @AuthenticationPrincipal AuthUser authUser) {
        boolean isLiked = likeService.toggleMenuLike(storeId, menuId, authUser.getUserId());
        return ResponseEntity.ok(SuccessResponse.of(isLiked));
    }

    @GetMapping("/stores/{storeId}/menus/{menuId}/like")
    public ResponseEntity<LikeResponseDto.Basic> getMenuLikeStatus(
            @PathVariable Long storeId,
            @PathVariable Long menuId,
            @AuthenticationPrincipal AuthUser authUser) {
        LikeResponseDto.Basic response = likeService.getMenuLikeStatus(storeId, menuId, authUser.getUserId());
        return ResponseEntity.ok(response);
    }
}