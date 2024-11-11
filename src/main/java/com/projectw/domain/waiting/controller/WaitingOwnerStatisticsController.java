package com.projectw.domain.waiting.controller;

import com.projectw.common.dto.SuccessResponse;
import com.projectw.domain.waiting.dto.WaitingStatisticsResponse;
import com.projectw.domain.waiting.service.WaitingStatisticsService;
import com.projectw.security.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class WaitingOwnerStatisticsController {

    private final WaitingStatisticsService statisticsService;

    @GetMapping("/v1/owner/stores/{storeId}/waitings/statistics/daily/range")
    public ResponseEntity<SuccessResponse<WaitingStatisticsResponse.DailyPage>> getDailyWaitingStatisticsBetweenDate(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable long storeId,
            @RequestParam LocalDate start,
            @RequestParam LocalDate end,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "30") int size) {
        return ResponseEntity.ok(SuccessResponse.of(statisticsService.getDailyWaitingStatisticsBetweenDate(authUser, storeId, start, end, page, size)));
    }

    @GetMapping("/v1/owner/stores/{storeId}/waitings/statistics/daily")
    public ResponseEntity<SuccessResponse<WaitingStatisticsResponse.Daily>> getDailyWaitingStatistics(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable long storeId,
            @RequestParam LocalDate date) {
        return ResponseEntity.ok(SuccessResponse.of(statisticsService.getDailyWaitingStatistics(authUser, storeId, date)));
    }

    @GetMapping("/v1/owner/stores/{storeId}/waitings/statistics/hourly/range")
    public ResponseEntity<SuccessResponse<List<WaitingStatisticsResponse.Hourly>>> getHourlyWaitingStatisticsByHourRange(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable long storeId,
            @RequestParam LocalDate date,
            @RequestParam int start,
            @RequestParam int end) {
        return ResponseEntity.ok(SuccessResponse.of(statisticsService.getHourlyWaitingStatisticsByHourRange(authUser, storeId, date, start, end)));
    }

    @GetMapping("/v1/owner/stores/{storeId}/waitings/statistics/hourly")
    public ResponseEntity<SuccessResponse<List<WaitingStatisticsResponse.Hourly>>> getHourlyWaitingStatistics(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable long storeId,
            @RequestParam LocalDate date) {
        return ResponseEntity.ok(SuccessResponse.of(statisticsService.getHourlyWaitingStatistics(authUser, storeId, date)));
    }
}
