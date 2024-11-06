package com.projectw.domain.dibs.controller;

import com.projectw.domain.dibs.dto.request.DibsRequestDto;
import com.projectw.domain.dibs.dto.response.DibsActionResponseDto;
import com.projectw.domain.dibs.dto.response.DibsResponseDto;
import com.projectw.domain.dibs.service.DibsService;
import com.projectw.security.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v2/user/dibs")
@RequiredArgsConstructor
public class DibsController {

    private final DibsService dibsService;

    @PostMapping
    public ResponseEntity<DibsActionResponseDto> addOrRemoveDibs(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestBody DibsRequestDto requestDto) {
        DibsActionResponseDto responseDto = dibsService.addOrRemoveDibs(authUser, requestDto);
        return ResponseEntity.ok(responseDto);
    }

    @GetMapping
    public ResponseEntity<List<DibsResponseDto>> getDibsList(
            @AuthenticationPrincipal AuthUser authUser) {
        List<DibsResponseDto> dibsList = dibsService.getDibsList(authUser);
        return ResponseEntity.ok(dibsList);
    }

    // 팔로우한 사용자의 찜 목록 조회
    @GetMapping("/following")
    public ResponseEntity<List<DibsResponseDto>> getFollowingDibsList(@AuthenticationPrincipal AuthUser authUser) {
        List<DibsResponseDto> followingDibsList = dibsService.getFollowingDibsList(authUser);
        return ResponseEntity.ok(followingDibsList);
    }
}
