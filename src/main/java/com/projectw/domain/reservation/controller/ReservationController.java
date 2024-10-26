package com.projectw.domain.reservation.controller;

import com.projectw.common.dto.SuccessResponse;
import com.projectw.common.enums.UserRole;
import com.projectw.domain.reservation.dto.ReserveRequest;
import com.projectw.domain.reservation.dto.ReserveResponse;
import com.projectw.domain.reservation.service.ReservationService;
import com.projectw.security.AuthUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import static com.projectw.common.constants.Const.FRONTEND_URL;

@Slf4j
@RequiredArgsConstructor
@RestController
@CrossOrigin(FRONTEND_URL)
@RequestMapping("/api/v1")
public class ReservationController {

    private final ReservationService reservationService;

    /**
     * method : reservationCancelReservation
     * memo   : 유저가 직접 결제 취소함
     * */
    @Secured({UserRole.Authority.USER})
    @PatchMapping("/store/{storeId}/reservation/{reservationId}")
    public ResponseEntity<SuccessResponse<Void>> reservationCancelReservation(
            @AuthenticationPrincipal AuthUser authUser ,
            @PathVariable Long storeId,
            @PathVariable Long reservationId,
            @Valid @RequestBody ReserveRequest.Cancel cancel) {
        reservationService.cancelReservation(authUser.getUserId() , storeId , reservationId , cancel);
        return ResponseEntity.ok(SuccessResponse.of(null));
    }

    @Secured({UserRole.Authority.USER})
    @GetMapping("/reservations")
    public ResponseEntity<SuccessResponse<Page<ReserveResponse.Infos>>> getUserReservations(
            @AuthenticationPrincipal AuthUser authUser ,
            @ModelAttribute ReserveRequest.Parameter parameter) {
        return ResponseEntity.ok(SuccessResponse.of(reservationService.getUserReservations(authUser.getUserId() , parameter)));
    }
}
