package com.projectw.common.resttemplate;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Component
public class TossPaymentsService {

    private final RestTemplate restTemplate;

    @Value("${TOSS_PAY_TEST_SECRET_KEY}")
    private String secretKey;

    private String token;

    @PostConstruct
    public void init() {
        String testSecretApiKey = secretKey + ":";
        token = "Basic " + new String(Base64.getEncoder().encode(testSecretApiKey.getBytes(StandardCharsets.UTF_8)));
    }

    /**
     * 결제 승인
     * */
    public ResponseEntity<String> confirm(String paymentKey, String orderId, String amount) {
        // 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        headers.set("Content-Type", "application/json; charset=UTF-8");

        String requestBody = String.format("{\"paymentKey\":\"%s\", \"orderId\":\"%s\", \"amount\":\"%s\"}", paymentKey, orderId, amount);

        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        String url = "https://api.tosspayments.com/v1/payments/confirm";

        // POST 요청 보내기
        return restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
    }

    /**
     * 결제 취소
     * */
    public ResponseEntity<String> cancel(String paymentKey , String cancelReason) {
        // 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        headers.set("Content-Type", "application/json; charset=UTF-8");

        String requestBody = String.format("{\"cancelReason\":\"%s\"}", cancelReason);

        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        String url = String.format("https://api.tosspayments.com/v1/payments/%s/cancel", paymentKey);

        // POST 보내기
        return restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
    }
}
