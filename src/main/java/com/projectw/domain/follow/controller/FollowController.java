package com.projectw.domain.follow.controller;

import com.projectw.domain.follow.dto.FollowResponseDto;
import com.projectw.domain.follow.dto.FollowUserDto;
import com.projectw.domain.follow.service.FollowService;
import com.projectw.security.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v2/user/follow")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;

    // 팔로우 추가/취소 메서드
    @PostMapping("/{targetUserId}")
    public ResponseEntity<FollowResponseDto> followOrUnfollow(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long targetUserId) {
        FollowResponseDto responseDto = followService.followOrUnfollow(authUser, targetUserId);
        return ResponseEntity.ok(responseDto);
    }

    // 팔로잉 목록 조회 (FollowUserDto 사용)
    @GetMapping("/following")
    public ResponseEntity<List<FollowUserDto>> getFollowingList(@AuthenticationPrincipal AuthUser authUser) {
        List<FollowUserDto> followingList = followService.getFollowingList(authUser);
        return ResponseEntity.ok(followingList);
    }

    // 팔로워 목록 조회 (FollowUserDto 사용)
    @GetMapping("/followers")
    public ResponseEntity<List<FollowUserDto>> getFollowerList(@AuthenticationPrincipal AuthUser authUser) {
        List<FollowUserDto> followerList = followService.getFollowerList(authUser);
        return ResponseEntity.ok(followerList);
    }
}