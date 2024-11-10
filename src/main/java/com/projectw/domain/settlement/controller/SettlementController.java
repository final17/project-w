package com.projectw.domain.settlement.controller;

import com.projectw.common.dto.SuccessResponse;
import com.projectw.common.enums.UserRole;
import com.projectw.domain.settlement.dto.SettlementRequest;
import com.projectw.domain.settlement.dto.SettlementResponse;
import com.projectw.domain.settlement.service.SettlementService;
import com.projectw.domain.settlement.service.SettlementSummaryService;
import com.projectw.security.AuthUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v2/store/{storeId}/settlement")
@RestController
public class SettlementController {

    private final SettlementService settlementService;
    private final SettlementSummaryService settlementSummaryService;

    @Secured({UserRole.Authority.OWNER})
    @GetMapping("/log")
    public ResponseEntity<SuccessResponse<Page<SettlementResponse.Log>>> getSettlementLog(
            @AuthenticationPrincipal AuthUser authUser ,
            @PathVariable Long storeId,
            @ModelAttribute SettlementRequest.Log log) {
        return ResponseEntity.ok(SuccessResponse.of(settlementService.getSettlementLog(authUser.getUserId() , storeId , log)));
    }

    @Secured({UserRole.Authority.OWNER})
    @GetMapping("/summary")
    public ResponseEntity<SuccessResponse<List<SettlementResponse.Summary>>> getSettlementSummary(
            @AuthenticationPrincipal AuthUser authUser ,
            @PathVariable Long storeId,
            @ModelAttribute SettlementRequest.Summary summary) {

        return ResponseEntity.ok(SuccessResponse.of(settlementSummaryService.getSettlementSummary(authUser.getUserId() , storeId , summary)));
    }

}
