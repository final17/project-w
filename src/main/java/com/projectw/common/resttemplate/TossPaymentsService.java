package com.projectw.common.resttemplate;

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

    public ResponseEntity<String> getPaymentDetails(String paymentKey, String orderId, String amount) {
        String testSecretApiKey = secretKey + ":";
        String encodedAuth = "Basic " + new String(Base64.getEncoder().encode(testSecretApiKey.getBytes(StandardCharsets.UTF_8)));

        log.info(encodedAuth);

        // 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", encodedAuth);
        headers.set("Content-Type", "application/json; charset=UTF-8");

        String requestBody = String.format("{\"paymentKey\":\"%s\", \"orderId\":\"%s\", \"amount\":\"%s\"}", paymentKey, orderId, amount);

        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        String url = "https://api.tosspayments.com/v1/payments/confirm";

        // GET 요청 보내기
        return restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
    }
}
