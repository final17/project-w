package com.projectw.domain.like.controller;

import com.projectw.common.dto.SuccessResponse;
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
        boolean isLiked = likeService.toggleLike(reviewId, user.getEmail());
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
        boolean hasLiked = likeService.hasLiked(reviewId, user.getEmail());
        long likeCount = likeService.getLikeCount(reviewId);

        Map<String, Object> response = new HashMap<>();
        response.put("liked", hasLiked);
        response.put("likeCount", likeCount);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/menus/{menuId}/like")
    public ResponseEntity<SuccessResponse<Boolean>> toggleLikeMenu(
            @PathVariable Long menuId,
            @AuthenticationPrincipal AuthUser authUser) {

        boolean isLiked = likeService.toggleLikeForMenu(menuId, authUser.getUserId());
        return ResponseEntity.ok(SuccessResponse.of(isLiked));
    }

    @GetMapping("/menus/{menuId}/likes")
    public ResponseEntity<SuccessResponse<Long>> getMenuLikeCount(@PathVariable Long menuId) {
        long likeCount = likeService.getLikeCountForMenu(menuId);
        return ResponseEntity.ok(SuccessResponse.of(likeCount));
    }

    @GetMapping("/menus/{menuId}/has-liked")
    public ResponseEntity<SuccessResponse<Boolean>> hasLikedMenu(
            @PathVariable Long menuId,
            @AuthenticationPrincipal AuthUser authUser) {

        boolean hasLiked = likeService.hasLikedMenu(menuId, authUser.getUserId());
        return ResponseEntity.ok(SuccessResponse.of(hasLiked));
    }
}
