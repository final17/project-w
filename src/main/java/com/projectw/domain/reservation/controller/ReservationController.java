package com.projectw.domain.reservation.controller;

import com.projectw.common.dto.SuccessResponse;
import com.projectw.common.enums.UserRole;
import com.projectw.domain.reservation.dto.ReserveRequest;
import com.projectw.domain.reservation.service.ReservationService;
import com.projectw.security.AuthUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/reservation")
public class ReservationController {

    private final ReservationService reservationService;

    @Secured({UserRole.Authority.USER})
    @PostMapping("/wait/{storedId}")
    public ResponseEntity<SuccessResponse<Void>> saveWait(
            @AuthenticationPrincipal AuthUser authUser ,
            @PathVariable Long storedId ,
            @RequestBody ReserveRequest.Wait wait) {
        log.info("saveWait");
        reservationService.saveWait(authUser.getUserId() , storedId , wait);
        return ResponseEntity.ok(SuccessResponse.of(null));
    }

    @Secured({UserRole.Authority.USER})
    @PostMapping("/reservation/{storeId}")
    public ResponseEntity<SuccessResponse<Void>> saveReservation(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long storeId ,
            @RequestBody ReserveRequest.Reservation reservation) {
        reservationService.saveReservation(authUser.getUserId() , storeId , reservation);
        return ResponseEntity.ok(SuccessResponse.of(null));
    }

    @Secured({UserRole.Authority.USER})
    @PatchMapping("/cancel/{reservationId}")
    public ResponseEntity<SuccessResponse<Void>> cancelReservation(
            @AuthenticationPrincipal AuthUser authUser ,
            @PathVariable Long reservationId) {
        reservationService.cancelReservation(authUser.getUserId() , reservationId);
        return ResponseEntity.ok(SuccessResponse.of(null));
    }

    @Secured({UserRole.Authority.OWNER})
    @PatchMapping("/refusal/{reservationId}")
    public ResponseEntity<SuccessResponse<Void>> refusalReservation(
            @AuthenticationPrincipal AuthUser authUser ,
            @PathVariable Long reservationId) {
        reservationService.refusalReservation(authUser.getUserId() , reservationId);
        return ResponseEntity.ok(SuccessResponse.of(null));
    }

    @Secured({UserRole.Authority.OWNER})
    @PatchMapping("/apply/{reservationId}")
    public ResponseEntity<SuccessResponse<Void>> applyReservation(
            @AuthenticationPrincipal AuthUser authUser ,
            @PathVariable Long reservationId) {
        reservationService.applyReservation(authUser.getUserId() , reservationId);
        return ResponseEntity.ok(SuccessResponse.of(null));
    }

    @Secured({UserRole.Authority.OWNER})
    @PatchMapping("/complete/{reservationId}")
    public ResponseEntity<SuccessResponse<Void>> completeReservation(
            @AuthenticationPrincipal AuthUser authUser ,
            @PathVariable Long reservationId) {
        reservationService.completeReservation(authUser.getUserId() , reservationId);
        return ResponseEntity.ok(SuccessResponse.of(null));
    }

    @Secured({UserRole.Authority.OWNER})
    @GetMapping("/onwer")
    public void getOnwerReservation(
            @AuthenticationPrincipal AuthUser authUser ,
            @ModelAttribute ReserveRequest.Parameter parameter) {
        reservationService.getOnwerReservation(authUser.getUserId() , parameter);
    }

    @Secured({UserRole.Authority.USER})
    @GetMapping("/user")
    public void getUserReservation(
            @AuthenticationPrincipal AuthUser authUser ,
            @ModelAttribute ReserveRequest.Parameter parameter) {
        reservationService.getUserReservation(authUser.getUserId() , parameter);
    }
}
