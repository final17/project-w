package com.projectw.domain.payment.controller;

import com.projectw.common.dto.SuccessResponse;
import com.projectw.domain.payment.dto.PaymentRequest;
import com.projectw.domain.payment.dto.PaymentResponse;
import com.projectw.domain.payment.service.PaymentService;
import com.projectw.domain.reservation.dto.ReserveRequest;
import com.projectw.security.AuthUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequestMapping("/api/v2/payment")
@RequiredArgsConstructor
@RestController
public class PaymentController {

    private final PaymentService paymentService;

    @CrossOrigin("http://localhost:3000")
    @GetMapping("/prepare")
    public ResponseEntity<SuccessResponse<PaymentResponse.Prepare>> prepare(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @ModelAttribute PaymentRequest.Prepare prepare) {
        return ResponseEntity.ok(SuccessResponse.of(paymentService.prepare(authUser.getUserId() , prepare)));
    }

    @GetMapping("/success")
    public ResponseEntity<SuccessResponse<PaymentResponse.Susscess>> success(
            @Valid @ModelAttribute PaymentRequest.Susscess success) throws Exception {
        paymentService.success(success);
        return null;
    }

    @GetMapping("/fail")
    public ResponseEntity<SuccessResponse<PaymentResponse.Fail>> fail(
            @Valid @ModelAttribute PaymentRequest.Fail fail) {
        return null;
    }


}
