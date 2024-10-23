package com.projectw.domain.reservation.controller;

import com.projectw.common.dto.SuccessResponse;
import com.projectw.common.enums.UserRole;
import com.projectw.domain.reservation.dto.ReserveRequest;
import com.projectw.domain.reservation.dto.ReserveResponse;
import com.projectw.domain.reservation.service.ReservationService;
import com.projectw.security.AuthUser;
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
@RequestMapping("/api/v1")
public class ReservationController {

    private final ReservationService reservationService;

    @Secured({UserRole.Authority.USER})
    @PostMapping("/store/{storeId}/wait")
    public ResponseEntity<SuccessResponse<Void>> saveWait(
            @AuthenticationPrincipal AuthUser authUser ,
            @PathVariable Long storeId ,
            @RequestBody ReserveRequest.Wait wait) {
        reservationService.saveWait(authUser.getUserId() , storeId , wait);
        return ResponseEntity.ok(SuccessResponse.of(null));
    }

    @Secured({UserRole.Authority.USER})
    @PostMapping("/store/{storeId}/reservation")
    public ResponseEntity<SuccessResponse<Void>> saveReservation(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long storeId ,
            @RequestBody ReserveRequest.Reservation reservation) {
        reservationService.saveReservation(authUser.getUserId() , storeId , reservation);
        return ResponseEntity.ok(SuccessResponse.of(null));
    }

    @Secured({UserRole.Authority.USER})
    @PatchMapping("/store/{storeId}/reservation/{reservationId}")
    public ResponseEntity<SuccessResponse<Void>> reservationCancelReservation(
            @AuthenticationPrincipal AuthUser authUser ,
            @PathVariable Long storeId,
            @PathVariable Long reservationId) {
        reservationService.reservationCancelReservation(authUser.getUserId() , storeId , reservationId);
        return ResponseEntity.ok(SuccessResponse.of(null));
    }

    @Secured({UserRole.Authority.USER})
    @PatchMapping("/store/{storeId}/wait/{reservationId}")
    public ResponseEntity<SuccessResponse<Void>> waitCancelReservation(
            @AuthenticationPrincipal AuthUser authUser ,
            @PathVariable Long storeId,
            @PathVariable Long reservationId) {
        reservationService.waitCancelReservation(authUser.getUserId() , storeId , reservationId);
        return ResponseEntity.ok(SuccessResponse.of(null));
    }

    @Secured({UserRole.Authority.USER})
    @GetMapping("/reservations")
    public ResponseEntity<SuccessResponse<Page<ReserveResponse.Infos>>> getUserReservations(
            @AuthenticationPrincipal AuthUser authUser ,
            @ModelAttribute ReserveRequest.Parameter parameter) {
        return ResponseEntity.ok(SuccessResponse.of(reservationService.getUserReservations(authUser.getUserId() , parameter)));
    }

    @Secured({UserRole.Authority.USER})
    @GetMapping("/store/{storeId}/info/{reservationId}")
    public ResponseEntity<SuccessResponse<ReserveResponse.Info>> getReservation(
            @AuthenticationPrincipal AuthUser authUser ,
            @PathVariable Long storeId,
            @PathVariable Long reservationId) {
        return ResponseEntity.ok(SuccessResponse.of(reservationService.getReservation(authUser.getUserId() , storeId , reservationId)));
    }
}
