package com.projectw.domain.follow.controller;

import com.projectw.domain.follow.dto.FollowResponseDto;
import com.projectw.domain.follow.dto.FollowUserDto;
import com.projectw.domain.follow.service.FollowService;
import com.projectw.domain.store.dto.StoreResponse;
import com.projectw.domain.store.service.StoreUserServiceImpl;
import com.projectw.security.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v2/user/follow")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;
    private final StoreUserServiceImpl storeUserService;

    /**
     * 팔로우 추가/취소 메서드 (토글 방식)
     */
    @PostMapping("/{targetUserId}")
    public ResponseEntity<FollowResponseDto> followOrUnfollow(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long targetUserId) {
        FollowResponseDto responseDto = followService.followOrUnfollow(authUser, targetUserId);
        return ResponseEntity.ok(responseDto);
    }

    /**
     * 팔로잉 목록 조회
     */
    @GetMapping("/following")
    public ResponseEntity<List<FollowUserDto.Basic>> getFollowingList(@AuthenticationPrincipal AuthUser authUser) {
        List<FollowUserDto.Basic> followingList = followService.getFollowingList(authUser);
        return ResponseEntity.ok(followingList);
    }

    /**
     * 팔로워 목록 조회
     */
    @GetMapping("/followers")
    public ResponseEntity<List<FollowUserDto.Basic>> getFollowerList(@AuthenticationPrincipal AuthUser authUser) {
        List<FollowUserDto.Basic> followerList = followService.getFollowerList(authUser);
        return ResponseEntity.ok(followerList);
    }

    /**
     * 팔로우한 사용자가 좋아요한 가게 조회
     */
    @GetMapping("/followed/liked")
    public ResponseEntity<Page<StoreResponse.Like>> getLikedStoresOfFollowedUsers(
            @AuthenticationPrincipal AuthUser authUser,
            Pageable pageable
    ) {
        Page<StoreResponse.Like> likedStores = storeUserService.getLikedStoresOfFollowedUsers(authUser, pageable);
        return ResponseEntity.ok(likedStores);
    }
}