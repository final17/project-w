package com.projectw.domain.reservation.controller;

import com.projectw.common.dto.SuccessResponse;
import com.projectw.common.enums.UserRole;
import com.projectw.domain.reservation.dto.ReserveRequest;
import com.projectw.domain.reservation.dto.ReserveResponse;
import com.projectw.domain.reservation.service.ReservationManagementService;
import com.projectw.security.AuthUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/reservation-management")
public class ReservationManagementController {

    private final ReservationManagementService reservationManagementService;

    @Secured({UserRole.Authority.OWNER})
    @PatchMapping("/{reservationId}/refusal")
    public ResponseEntity<SuccessResponse<Void>> refusalReservation(
            @AuthenticationPrincipal AuthUser authUser ,
            @PathVariable Long reservationId ,
            @Valid @RequestBody ReserveRequest.Cancel cancel) {
        reservationManagementService.refusalReservation(authUser.getUserId() , reservationId , cancel);
        return ResponseEntity.ok(SuccessResponse.of(null));
    }

    @Secured({UserRole.Authority.OWNER})
    @PatchMapping("/{reservationId}/apply")
    public ResponseEntity<SuccessResponse<Void>> applyReservation(
            @AuthenticationPrincipal AuthUser authUser ,
            @PathVariable Long reservationId) {
        reservationManagementService.applyReservation(authUser.getUserId() , reservationId);
        return ResponseEntity.ok(SuccessResponse.of(null));
    }

    @Secured({UserRole.Authority.OWNER})
    @PatchMapping("/{reservationId}/complete")
    public ResponseEntity<SuccessResponse<Void>> completeReservation(
            @AuthenticationPrincipal AuthUser authUser ,
            @PathVariable Long reservationId) {
        reservationManagementService.completeReservation(authUser.getUserId() , reservationId);
        return ResponseEntity.ok(SuccessResponse.of(null));
    }

    @Secured({UserRole.Authority.OWNER})
    @GetMapping
    public ResponseEntity<SuccessResponse<Page<ReserveResponse.Infos>>> getOnwerReservations(
            @AuthenticationPrincipal AuthUser authUser ,
            @ModelAttribute ReserveRequest.Parameter parameter) {
        return ResponseEntity.ok(SuccessResponse.of(reservationManagementService.getOnwerReservations(authUser.getUserId() , parameter)));
    }

}
