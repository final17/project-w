package com.projectw.domain.payment.controller;

import com.projectw.common.dto.SuccessResponse;
import com.projectw.domain.payment.dto.PaymentRequest;
import com.projectw.domain.payment.dto.PaymentResponse;
import com.projectw.domain.payment.service.PaymentService;
import com.projectw.security.AuthUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @GetMapping("/prepare")
    public ResponseEntity<SuccessResponse<PaymentResponse.Prepare>> prepare(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @ModelAttribute PaymentRequest.Prepare prepare) {
        return ResponseEntity.ok(SuccessResponse.of(paymentService.prepare(authUser.getUserId() , prepare)));
    }

    @GetMapping("/success")
    public RedirectView success(
            @Valid @ModelAttribute PaymentRequest.Susscess success) throws Exception {;
        return paymentService.success(success);
    }

    @GetMapping("/fail")
    public RedirectView fail(
            @Valid @ModelAttribute PaymentRequest.Fail fail) {
        return paymentService.fail(fail);
    }


}
