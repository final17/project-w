//package com.projectw.domain.payment;
//
//import com.fasterxml.jackson.databind.JsonNode;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.HttpEntity;
//import org.springframework.stereotype.Service;
//
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.projectw.domain.payment.entity.TossPaymentResponse;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.client.RestTemplate;
//
//import javax.smartcardio.Card;
//import java.nio.charset.StandardCharsets;
//import java.time.OffsetDateTime;
//import java.time.format.DateTimeFormatter;
//import java.util.Base64;
//import java.util.UUID;
//
//@Service
//@RequiredArgsConstructor
//public class PaymentService {
//
//    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);
//    private final TempOrderRepository tempOrderRepository;
//
//    // @Value("${PAYMENTS_TOSS_TEST_SECRET_KEY}")
//    private String secretKey;
//
//    private final RestTemplate restTemplate;
//
//    public TossPaymentResponse onSuccessPay(String oid, Integer amount, String pmkey) throws Exception {
//
//        String secretKeyWithColon = secretKey + ":";
//        String encryptedSecretKey = "Basic " + Base64.getEncoder().encodeToString(secretKeyWithColon.getBytes(StandardCharsets.UTF_8));
//
//        // 멱등성 키
//        String idempotencyKey = UUID.randomUUID().toString();
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.set("Authorization", encryptedSecretKey);
//        headers.set("Content-Type", "application/json; charset=UTF-8");
//        headers.set("Idempotency-Key", idempotencyKey);
//
//        String requestBody = String.format("{\"paymentKey\":\"%s\", \"orderId\":\"%s\", \"amount\":%d}", pmkey, oid, amount);
//
//        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
//
//        String url = "https://api.tosspayments.com/v1/payments/confirm";
//
//        ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
//
//        ObjectMapper mapper = new ObjectMapper();
//
//        String responseBody = responseEntity.getBody();
//        JsonNode jsonNode = mapper.readTree(responseBody);
//
//        // https://docs.tosspayments.com/reference#%EC%A0%95%EC%82%B0
//        if(responseEntity.getStatusCode() == HttpStatus.OK) {
//            String mId = jsonNode.get("mId").textValue();
//            String paymentKey = jsonNode.get("paymentKey").textValue();
//            String orderId = jsonNode.get("orderId").textValue();
//            String orderName = jsonNode.get("orderName").textValue();
//            String status = jsonNode.get("status").textValue();
//            String method = jsonNode.get("method").textValue();
//            int balanceAmount = jsonNode.get("balanceAmount").intValue();
//            int totalAmount = jsonNode.get("totalAmount").intValue();
//            int suppliedAmount = jsonNode.get("suppliedAmount").intValue();
//            int vat = jsonNode.get("vat").intValue();
//            String currency = jsonNode.get("currency").textValue();
//            boolean isPartialCancelable = jsonNode.get("isPartialCancelable").booleanValue();
//
//            // OffsetDateTime으로 변환
//            DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
//            OffsetDateTime requestedAt = OffsetDateTime.parse(jsonNode.get("requestedAt").textValue(), formatter);
//            OffsetDateTime approvedAt = OffsetDateTime.parse(jsonNode.get("approvedAt").textValue(), formatter);
//
//            // Card 정보가 있는 경우 처리
//            Card card = null;
//            if (jsonNode.has("card")) {
//                if(!jsonNode.get("card").isNull()){
//                    JsonNode cardNode = jsonNode.get("card");
//                    card = Card.builder()
//                            .amount(cardNode.get("amount").intValue())
//                            .issuerCode(cardNode.get("issuerCode").textValue())
//                            .acquirerCode(cardNode.get("acquirerCode").textValue())
//                            .number(cardNode.get("number").textValue())
//                            .installmentPlanMonths(cardNode.get("installmentPlanMonths").intValue())
//                            .approveNo(cardNode.get("approveNo").textValue())
//                            .useCardPoint(cardNode.get("useCardPoint").booleanValue())
//                            .cardType(cardNode.get("cardType").textValue())
//                            .ownerType(cardNode.get("ownerType").textValue())
//                            .acquireStatus(cardNode.get("acquireStatus").textValue())
//                            .isInterestFree(cardNode.get("isInterestFree").booleanValue())
//                            .interestPayer(cardNode.get("interestPayer").isNull() ? null : cardNode.get("interestPayer").textValue())
//                            .build();
//                }
//            }
//
//            // EasyPay 정보가 있는 경우 처리
//            EasyPay easyPay = null;
//            if (jsonNode.has("easyPay")) {
//                if(!jsonNode.get("easyPay").isNull()){
//                    JsonNode easyPayNode = jsonNode.get("easyPay");
//                    easyPay = EasyPay.builder()
//                            .provider(easyPayNode.get("provider").textValue())
//                            .easyPayAmount(easyPayNode.get("amount").intValue())
//                            .easyPayDiscountAmount(easyPayNode.get("discountAmount").intValue())
//                            .build();
//                }
//            }
//
//            // TossPaymentResponse 객체 빌더로 생성
//            return TossPaymentResponse.builder()
//                    .mId(mId)
//                    .paymentKey(paymentKey)
//                    .orderId(orderId)
//                    .orderName(orderName)
//                    .status(status)
//                    .method(method)
//                    .balanceAmount(balanceAmount)
//                    .totalAmount(totalAmount)
//                    .suppliedAmount(suppliedAmount)
//                    .vat(vat)
//                    .currency(currency)
//                    .isPartialCancelable(isPartialCancelable)
//                    .requestedAt(requestedAt)
//                    .approvedAt(approvedAt)
//                    .card(card)
//                    .easyPay(easyPay)
//                    .build();
//
//        } else {
//            String errCode = jsonNode.get("code").textValue();
//            String errMessage = jsonNode.get("message").textValue();
//            log.error("tosspayments errorCode: {}", errCode);
//            log.error("tosspayments errorMessage: {}", errMessage);
//        }
//
//        return new TossPaymentResponse();
//    }
//}
