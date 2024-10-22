package com.projectw.domain.reservation.controller;

import com.projectw.common.dto.SuccessResponse;
import com.projectw.common.enums.UserRole;
import com.projectw.domain.reservation.dto.ReserveRequest;
import com.projectw.domain.reservation.dto.ReserveResponse;
import com.projectw.domain.reservation.service.ReservationService;
import com.projectw.security.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ReservationOwnerController {

    private final ReservationService reservationService;

    @Secured({ UserRole.Authority.OWNER })
    @PatchMapping("/api/stores/{storeId}/reservations/{reservationId}/status")
    public ResponseEntity<SuccessResponse<ReserveResponse.Info>> changeStatus(
            @PathVariable long storeId,
            @PathVariable long reservationId,
            @AuthenticationPrincipal AuthUser user,
            @RequestBody ReserveRequest.UpdateStatus request) {

        return ResponseEntity.ok(reservationService.reservationStatusChange(user,storeId, reservationId, request));
    }
}
