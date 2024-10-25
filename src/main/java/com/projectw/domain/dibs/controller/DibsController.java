package com.projectw.domain.dibs.controller;

import com.projectw.domain.dibs.dto.request.DibsRequestDto;
import com.projectw.domain.dibs.dto.response.DibsActionResponseDto;
import com.projectw.domain.dibs.dto.response.DibsResponseDto;
import com.projectw.domain.dibs.service.DibsService;
import com.projectw.security.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v2/user/dibs")
@RequiredArgsConstructor
public class DibsController {

    @Autowired
    private DibsService dibsService;

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
}
