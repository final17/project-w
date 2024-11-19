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

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v2")
public class ReservationController {

    private final ReservationService reservationService;

    /**
     * method : reservationCancelReservation
     * memo   : 유저가 직접 결제 취소함
     * */
    @PatchMapping("/store/{storeId}/reservation/{reservationId}")
    public ResponseEntity<SuccessResponse<Void>> reservationCancelReservation(
            @AuthenticationPrincipal AuthUser authUser ,
            @PathVariable Long storeId,
            @PathVariable Long reservationId,
            @Valid @RequestBody ReserveRequest.Cancel cancel) {
        reservationService.cancelReservation(authUser.getUserId() , storeId , reservationId , cancel);
        return ResponseEntity.ok(SuccessResponse.of(null));
    }

    @GetMapping("/reservations")
    public ResponseEntity<SuccessResponse<Page<ReserveResponse.Infos>>> getReservations(
            @AuthenticationPrincipal AuthUser authUser ,
            @ModelAttribute ReserveRequest.Parameter parameter) {
        return ResponseEntity.ok(SuccessResponse.of(reservationService.getReservations(authUser.getUserId() , parameter)));
    }

    @GetMapping("/reservations/{reservationId}/menu")
    public ResponseEntity<SuccessResponse<List<ReserveResponse.Carts>>> getReservationMenus(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long reservationId
    ) {
        List<ReserveResponse.Carts> menus = reservationService.getReservationMenus(authUser.getUserId(), reservationId);
        return ResponseEntity.ok(SuccessResponse.of(menus));
    }


}
