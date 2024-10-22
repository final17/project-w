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
@RequestMapping("/api/v1/reservation")
// TODO : user 용 onwer 용 따로 컨트롤러로 빼서 진행 예정
// user의 경우 : /api/v1/store/{storeId}/reservation
// onwer의 경우 : /api/v1/reservation-management
public class ReservationController {

    private final ReservationService reservationService;

    @Secured({UserRole.Authority.USER})
    @PostMapping("/wait/{storedId}")
    public ResponseEntity<SuccessResponse<Void>> saveWait(
            @AuthenticationPrincipal AuthUser authUser ,
            @PathVariable Long storedId ,
            @RequestBody ReserveRequest.Wait wait) {
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
    @PatchMapping("/{reservationId}/reservation-cancel")
    public ResponseEntity<SuccessResponse<Void>> reservationCancelReservation(
            @AuthenticationPrincipal AuthUser authUser ,
            @PathVariable Long reservationId) {
        reservationService.reservationCancelReservation(authUser.getUserId() , reservationId);
        return ResponseEntity.ok(SuccessResponse.of(null));
    }

    @Secured({UserRole.Authority.USER})
    @PatchMapping("/{reservationId}/wait-cancel")
    public ResponseEntity<SuccessResponse<Void>> waitCancelReservation(
            @AuthenticationPrincipal AuthUser authUser ,
            @PathVariable Long reservationId) {
        reservationService.waitCancelReservation(authUser.getUserId() , reservationId);
        return ResponseEntity.ok(SuccessResponse.of(null));
    }

    @Secured({UserRole.Authority.OWNER})
    @PatchMapping("/{reservationId}/refusal")
    public ResponseEntity<SuccessResponse<Void>> refusalReservation(
            @AuthenticationPrincipal AuthUser authUser ,
            @PathVariable Long reservationId) {
        reservationService.refusalReservation(authUser.getUserId() , reservationId);
        return ResponseEntity.ok(SuccessResponse.of(null));
    }

    @Secured({UserRole.Authority.OWNER})
    @PatchMapping("/{reservationId}/apply")
    public ResponseEntity<SuccessResponse<Void>> applyReservation(
            @AuthenticationPrincipal AuthUser authUser ,
            @PathVariable Long reservationId) {
        reservationService.applyReservation(authUser.getUserId() , reservationId);
        return ResponseEntity.ok(SuccessResponse.of(null));
    }

    @Secured({UserRole.Authority.OWNER})
    @PatchMapping("/{reservationId}/complete")
    public ResponseEntity<SuccessResponse<Void>> completeReservation(
            @AuthenticationPrincipal AuthUser authUser ,
            @PathVariable Long reservationId) {
        reservationService.completeReservation(authUser.getUserId() , reservationId);
        return ResponseEntity.ok(SuccessResponse.of(null));
    }

    @Secured({UserRole.Authority.OWNER})
    @GetMapping("/onwer")
    public ResponseEntity<SuccessResponse<Page<ReserveResponse.Infos>>> getOnwerReservations(
            @AuthenticationPrincipal AuthUser authUser ,
            @ModelAttribute ReserveRequest.Parameter parameter) {
        return ResponseEntity.ok(SuccessResponse.of(reservationService.getOnwerReservations(authUser.getUserId() , parameter)));
    }

    @Secured({UserRole.Authority.USER})
    @GetMapping("/user")
    public ResponseEntity<SuccessResponse<Page<ReserveResponse.Infos>>> getUserReservations(
            @AuthenticationPrincipal AuthUser authUser ,
            @ModelAttribute ReserveRequest.Parameter parameter) {
        return ResponseEntity.ok(SuccessResponse.of(reservationService.getUserReservations(authUser.getUserId() , parameter)));
    }

    @Secured({UserRole.Authority.USER})
    @GetMapping("/{reservationId}/info")
    public ResponseEntity<SuccessResponse<ReserveResponse.Info>> getReservation(
            @AuthenticationPrincipal AuthUser authUser ,
            @PathVariable Long reservationId) {
        return ResponseEntity.ok(SuccessResponse.of(reservationService.getReservation(authUser.getUserId() , reservationId)));
    }
}
