package com.projectw.domain.payment.controller;

import com.projectw.common.dto.SuccessResponse;
import com.projectw.domain.payment.dto.PaymentRequest;
import com.projectw.domain.payment.dto.PaymentResponse;
import com.projectw.domain.payment.service.PaymentService;
import com.projectw.security.AuthUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

@Slf4j
@RequestMapping("/api/v2/payment")
@RequiredArgsConstructor
@RestController
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * 결제 요청 전 검증 단계!
     * */
    @GetMapping("/prepare")
    public ResponseEntity<SuccessResponse<PaymentResponse.Prepare>> prepare(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @ModelAttribute PaymentRequest.Prepare prepare) {
        return ResponseEntity.ok(SuccessResponse.of(paymentService.prepare(authUser.getUserId() , prepare)));
    }

    /**
     * 결제 승인 성공시
     * */
    @GetMapping("/success")
    public RedirectView success(
            @Valid @ModelAttribute PaymentRequest.Susscess success) throws Exception {;
        return paymentService.success(success);
    }

    /**
     * 결제 승인 실패시
     * */
    @GetMapping("/fail")
    public RedirectView fail(
            @Valid @ModelAttribute PaymentRequest.Fail fail) {
        return paymentService.fail(fail);
    }

    /**
     * 결제내역 조회! 성공 , 취소건!
     * */
    @GetMapping
    public ResponseEntity<SuccessResponse<Page<PaymentResponse.Payment>>> getPayments(
            @AuthenticationPrincipal AuthUser authUser ,
            @ModelAttribute PaymentRequest.Payment payment) {
        return ResponseEntity.ok(SuccessResponse.of(paymentService.getPayments(authUser.getUserId() , payment)));
    }

}
