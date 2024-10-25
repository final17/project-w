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
@CrossOrigin("http://localhost:3000")
@RequestMapping("/api/v1")
public class ReservationController {

    private final ReservationService reservationService;

    /**
     * method : preparePayments
     * memo   : 결제전 예약테이블에 값 넣기
     * */
    @Secured({UserRole.Authority.USER})
    @PostMapping("/store/{storeId}/payments/prepare")
    public ResponseEntity<SuccessResponse<ReserveResponse.ReservationInfo>> preparePayments(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long storeId ,
            @RequestBody ReserveRequest.Reservation reservation) {
        return ResponseEntity.ok(SuccessResponse.of(reservationService.preparePayments(authUser.getUserId() , storeId , reservation)));
    }

    /**
     * method : checkoutPayments
     * memo   : 결제완료
     * */
    @Secured({UserRole.Authority.USER})
    @PostMapping("/store/{storeId}/payments/checkout")
    public ResponseEntity<SuccessResponse<Void>> checkoutPayments(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long storeId,
            @RequestBody ReserveRequest.PaymentInfo paymentInfo) {
        // 추가적으로 값을 더 받아와야함!
        reservationService.checkoutPayments(authUser.getUserId() , storeId , paymentInfo);
        return ResponseEntity.ok(SuccessResponse.of(null));
    }

    /**
     * method : reservationCancelReservation
     * memo   : 유저가 직접 결제 취소함
     * */
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
    @GetMapping("/reservations")
    public ResponseEntity<SuccessResponse<Page<ReserveResponse.Infos>>> getUserReservations(
            @AuthenticationPrincipal AuthUser authUser ,
            @ModelAttribute ReserveRequest.Parameter parameter) {
        return ResponseEntity.ok(SuccessResponse.of(reservationService.getUserReservations(authUser.getUserId() , parameter)));
    }
}
