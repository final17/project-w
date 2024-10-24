package com.projectw.domain.like.controller;

import com.projectw.domain.like.service.LikeService;
import com.projectw.security.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v2/reviews/{reviewId}/likes")
@RequiredArgsConstructor
public class LikeController {
    private final LikeService likeService;

    @PatchMapping
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

    @GetMapping
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
}
