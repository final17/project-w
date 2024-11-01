package com.projectw.domain.payment.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.projectw.common.enums.ResponseCode;
import com.projectw.common.exceptions.NotFoundException;
import com.projectw.common.exceptions.PaymentNotFoundException;
import com.projectw.common.resttemplate.TossPaymentsService;
import com.projectw.domain.payment.dto.PaymentRequest;
import com.projectw.domain.payment.dto.PaymentResponse;
import com.projectw.domain.payment.entity.Payment;
import com.projectw.domain.payment.entity.PaymentCancel;
import com.projectw.domain.payment.entity.PaymentFail;
import com.projectw.domain.payment.entity.PaymentSuccess;
import com.projectw.domain.payment.entity.embeddables.Cancels;
import com.projectw.domain.payment.entity.embeddables.Card;
import com.projectw.domain.payment.entity.embeddables.EasyPay;
import com.projectw.domain.payment.enums.PaymentMethod;
import com.projectw.domain.payment.enums.PaymentStatus;
import com.projectw.domain.payment.enums.PaymentType;
import com.projectw.domain.payment.enums.Status;
import com.projectw.domain.payment.exception.InsufficientSeatsException;
import com.projectw.domain.payment.repository.PaymentCancelRepository;
import com.projectw.domain.payment.repository.PaymentFailRepository;
import com.projectw.domain.payment.repository.PaymentRepository;
import com.projectw.domain.payment.repository.PaymentSuccessRepository;
import com.projectw.domain.reservation.component.ReservationCheckService;
import com.projectw.domain.reservation.event.ReservationInsertEvent;
import com.projectw.domain.reservation.event.ReservationPaymentCompEvent;
import com.projectw.domain.store.entity.Store;
import com.projectw.domain.store.repository.StoreRepository;
import com.projectw.domain.user.entity.User;
import com.projectw.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.view.RedirectView;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentSuccessRepository paymentSuccessRepository;
    private final PaymentCancelRepository paymentCancelRepository;
    private final PaymentFailRepository paymentFailRepository;
    private final PaymentRepository paymentRepository;

    private final TossPaymentsService tossPaymentsService;

    private final UserRepository userRepository;
    private final StoreRepository storeRepository;

    // 예약 관련 검증
    private final ReservationCheckService reservationCheckService;

    private final ApplicationEventPublisher eventPublisher;

    private static final String PREFIX_ORDER_ID = "ORDER-";

    @Value("${front-end.url}")
    private String frontendUrl;

    /**
     * 결제창 열기전 검증 단계!
     * */
    @Transactional
    public PaymentResponse.Prepare prepare(Long userId , PaymentRequest.Prepare prepare) {
        // 현재시간대를 기준으로 예약 가능한 시간 값이 들어왔는지 검증
        reservationCheckService.isReservationDateValid(prepare.date() , prepare.time());

        // 유저 있는지?
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException(ResponseCode.NOT_FOUND_USER));
        // 식당이 있는지?
        Store store = storeRepository.findById(prepare.storeId()).orElseThrow(() -> new NotFoundException(ResponseCode.NOT_FOUND_STORE));

        // 들어온 금액과 내부의 설정된 금액이 다를때!
        reservationCheckService.validateDepositAmount(store , prepare.amount());

        // 본인 식당에 예약 가능한지?
        reservationCheckService.validateUserAuthorization(store , user);

        // 예약 가능한 시간대인지? 어떻게 처리할지 고민할 것
        reservationCheckService.validateReservationTime(store , prepare.time());

        boolean reservationYN = reservationCheckService.checkReservationCapacity(store , prepare.date() , prepare.time());

        if (reservationYN) {
            // 예약 성공
            String orderId = PREFIX_ORDER_ID + UUID.randomUUID().toString().substring(0, 8);

            Payment payment = Payment.builder()
                    .orderId(orderId)
                    .orderName(prepare.orderName())
                    .amount(prepare.amount())
                    .status(Status.PENDING)
                    .user(user)
                    .store(store)
                    .build();

            paymentRepository.save(payment);

            // ReservationEventListener 예약건 저장!
            ReservationInsertEvent reservationInsertEvent = new ReservationInsertEvent(orderId , prepare.date() , prepare.time() , prepare.numberPeople() , false , prepare.amount() , user , store);
            eventPublisher.publishEvent(reservationInsertEvent);

            return new PaymentResponse.Prepare(orderId , prepare.orderName(), prepare.amount());
        } else {
            // 예약 실패
            throw new InsufficientSeatsException(ResponseCode.INSUFFICIENT_SEAT);
        }
    }

    /**
     * 결제요청 성공
     * */
    @Transactional
    public RedirectView success(PaymentRequest.Susscess susscess) throws Exception {
        // 예약 가능한지 검증!
        Payment payment = paymentRepository.findByOrderIdAndStatus(susscess.orderId() , Status.PENDING).orElseThrow(() -> new PaymentNotFoundException(ResponseCode.PAYMENT_NOT_FOUND));

        ResponseEntity<String> responseEntity;
        String responseBody;
        JsonNode jsonNode;
        ObjectMapper mapper = new ObjectMapper();

        RedirectView redirectView = new RedirectView();

        responseEntity = tossPaymentsService.confirm(susscess.paymentKey() , susscess.orderId() , String.valueOf(susscess.amount()));
        responseBody = responseEntity.getBody();
        jsonNode = mapper.readTree(responseBody);
        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            confirmPayment(jsonNode);

            // 완료로 변경
            payment.updateStatus(Status.COMPLETED);

            // ReservationEventListener 예약을 결제완료로 변경
            ReservationPaymentCompEvent reservationPaymentCompEvent = new ReservationPaymentCompEvent(susscess.orderId());
            eventPublisher.publishEvent(reservationPaymentCompEvent);

            redirectView.setUrl(frontendUrl+"/payment/success?paymentKey=" + susscess.paymentKey() + "&orderId=" + susscess.orderId() + "&amount=" + String.valueOf(susscess.amount()));
        } else {
            String errCode = jsonNode.get("code").textValue();
            String errMessage = jsonNode.get("message").textValue();
            failPayment(susscess.orderId() , errCode , errMessage);

            // 취소로 변경
            payment.updateStatus(Status.CANCELLED);

            redirectView.setUrl(frontendUrl+"/payment/fail?code="+errCode+"&message='"+errMessage+"'");
        }

        return redirectView;
    }

    /**
     * 결제요청 실패
     * */
    public RedirectView fail(PaymentRequest.Fail fail) {
        failPayment(null , fail.code() , fail.message());
        RedirectView redirectView = new RedirectView();
        redirectView.setUrl(frontendUrl+"/payment/fail?code="+fail.code()+"&message='"+fail.message()+"'");
        return redirectView;
    }

    /**
     * 결제취소 요청
     * */
    @Transactional
    public void cancel(String orderId , String cancelReason) throws Exception {
        // 결제 정보 조회
        PaymentSuccess paymentSuccess = paymentSuccessRepository.findByOrderId(orderId).orElseThrow(() -> new NotFoundException(ResponseCode.PAYMENT_NOT_FOUND));

        // 결제 취소 요청(TOSS)
        ResponseEntity<String> responseEntity = tossPaymentsService.cancel(paymentSuccess.getPaymentKey() , cancelReason);
        ObjectMapper mapper = new ObjectMapper();
        String responseBody = responseEntity.getBody();
        JsonNode jsonNode = mapper.readTree(responseBody);

        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            // 취소 데이터 insert
            cancelPayment(jsonNode);

            // 취소로 변경
            Payment payment = paymentRepository.findByOrderIdAndStatus(orderId , Status.COMPLETED).orElseThrow(() -> new PaymentNotFoundException(ResponseCode.PAYMENT_NOT_FOUND));
            payment.updateStatus(Status.CANCELLED);
        } else {
            // 실패시 에러 저장
            String errCode = jsonNode.get("code").textValue();
            String errMessage = jsonNode.get("message").textValue();
            failPayment(orderId , errCode , errMessage);
        }
    }

    @Transactional
    public void timeoutCancel(String orderId) {
        Payment payment = paymentRepository.findByOrderIdAndStatus(orderId , Status.PENDING).orElseThrow(() -> new PaymentNotFoundException(ResponseCode.PAYMENT_NOT_FOUND));
        payment.updateStatus(Status.CANCELLED);
    }

    /**
     * 결제 에러 들어와야 하는 메서드!
     * */
    private void failPayment(String orderId , String code , String message) {
        // 어떤 에러인지 log에 표기
        log.error("tosspayments errorCode: {}", code);
        log.error("tosspayments errorMessage: {}", message);

        PaymentFail paymentFail = new PaymentFail(orderId , code , message);
        paymentFailRepository.save(paymentFail);
    }

    /**
     * 예약 승인 데이터 정리하여 insert 하는 메서드
     * */
    private void confirmPayment(JsonNode jsonNode) {
        String version = jsonNode.get("version").textValue();
        String paymentKey = jsonNode.get("paymentKey").textValue();
        PaymentType type = PaymentType.of(jsonNode.get("type").textValue());
        String orderId = jsonNode.get("orderId").textValue();
        String mId = jsonNode.get("mId").textValue();
        String currency = jsonNode.get("currency").textValue();
        PaymentMethod method = PaymentMethod.of(jsonNode.get("method").textValue());
        Long totalAmount = jsonNode.get("totalAmount").asLong();
        PaymentStatus status = PaymentStatus.of(jsonNode.get("status").textValue());

        DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
        OffsetDateTime requestedAt = OffsetDateTime.parse(jsonNode.get("requestedAt").textValue(), formatter);

        // Card 정보가 있으면 처리
        Card card = null;
        if (jsonNode.has("card")) {
            JsonNode cardNode = jsonNode.get("card");
            card = Card.builder()
                    .amount(cardNode.get("amount").intValue())
                    .issuerCode(cardNode.get("issuerCode").textValue())
                    .number(cardNode.get("number").textValue())
                    .installmentPlanMonths(cardNode.get("installmentPlanMonths").intValue())
                    .approveNo(cardNode.get("approveNo").textValue())
                    .useCardPoint(cardNode.get("useCardPoint").booleanValue())
                    .cardType(cardNode.get("cardType").textValue())
                    .ownerType(cardNode.get("ownerType").textValue())
                    .acquireStatus(cardNode.get("acquireStatus").textValue())
                    .isInterestFree(cardNode.get("isInterestFree").booleanValue())
                    .acquirerCode(cardNode.get("acquirerCode").isNull() ? null : cardNode.get("acquirerCode").textValue())
                    .interestPayer(cardNode.get("interestPayer").isNull() ? null : cardNode.get("interestPayer").textValue())
                    .build();
        }

        // EasyPay 정보가 있는 경우 처리
        EasyPay easyPay = null;
        if (jsonNode.has("easyPay")) {
            if(!jsonNode.get("easyPay").isNull()){
                JsonNode easyPayNode = jsonNode.get("easyPay");
                easyPay = EasyPay.builder()
                        .provider(easyPayNode.get("provider").textValue())
                        .easyPayAmount(easyPayNode.get("amount").intValue())
                        .easyPayDiscountAmount(easyPayNode.get("discountAmount").intValue())
                        .build();
            }
        }

        // 저장할 데이터
        PaymentSuccess paymentSuccess = PaymentSuccess.builder()
                .version(version)
                .paymentKey(paymentKey)
                .type(type)
                .orderId(orderId)
                .mId(mId)
                .currency(currency)
                .method(method)
                .totalAmount(totalAmount)
                .status(status)
                .requestedAt(requestedAt)
                .card(card)
                .easyPay(easyPay)
                .build();

        paymentSuccessRepository.save(paymentSuccess);
    }

    /**
     * 예약 취소 데이터 정리하여 insert 하는 메서드
     * */
    private void cancelPayment(JsonNode jsonNode) {
        String version = jsonNode.get("version").textValue();
        String paymentKey = jsonNode.get("paymentKey").textValue();
        PaymentType type = PaymentType.of(jsonNode.get("type").textValue());
        String orderId = jsonNode.get("orderId").textValue();
        String mId = jsonNode.get("mId").textValue();
        String currency = jsonNode.get("currency").textValue();
        PaymentMethod method = PaymentMethod.of(jsonNode.get("method").textValue());
        Long totalAmount = jsonNode.get("totalAmount").asLong();
        PaymentStatus status = PaymentStatus.of(jsonNode.get("status").textValue());

        DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
        OffsetDateTime requestedAt = OffsetDateTime.parse(jsonNode.get("requestedAt").textValue(), formatter);

        // Card 정보가 있으면 처리
        Card card = null;
        if (jsonNode.has("card")) {
            JsonNode cardNode = jsonNode.get("card");
            card = Card.builder()
                    .amount(cardNode.get("amount").intValue())
                    .issuerCode(cardNode.get("issuerCode").textValue())
                    .number(cardNode.get("number").textValue())
                    .installmentPlanMonths(cardNode.get("installmentPlanMonths").intValue())
                    .approveNo(cardNode.get("approveNo").textValue())
                    .useCardPoint(cardNode.get("useCardPoint").booleanValue())
                    .cardType(cardNode.get("cardType").textValue())
                    .ownerType(cardNode.get("ownerType").textValue())
                    .acquireStatus(cardNode.get("acquireStatus").textValue())
                    .isInterestFree(cardNode.get("isInterestFree").booleanValue())
                    .acquirerCode(cardNode.get("acquirerCode").isNull() ? null : cardNode.get("acquirerCode").textValue())
                    .interestPayer(cardNode.get("interestPayer").isNull() ? null : cardNode.get("interestPayer").textValue())
                    .build();
        }

        // EasyPay 정보가 있는 경우 처리
        EasyPay easyPay = null;
        if (jsonNode.has("easyPay")) {
            if(!jsonNode.get("easyPay").isNull()){
                JsonNode easyPayNode = jsonNode.get("easyPay");
                easyPay = EasyPay.builder()
                        .provider(easyPayNode.get("provider").textValue())
                        .easyPayAmount(easyPayNode.get("amount").intValue())
                        .easyPayDiscountAmount(easyPayNode.get("discountAmount").intValue())
                        .build();
            }
        }

        Cancels cancels = null;
        if (jsonNode.has("cancels") && jsonNode.get("cancels").isArray()) {
            for (JsonNode cancelsNode : jsonNode.get("cancels")) {
                cancels = Cancels.builder()
                        .transactionKey(cancelsNode.get("transactionKey").textValue())
                        .cancelReason(cancelsNode.get("cancelReason").textValue())
                        .canceledAt(OffsetDateTime.parse(cancelsNode.get("canceledAt").textValue(), formatter))
                        .cancelEasyPayDiscountAmount(cancelsNode.get("easyPayDiscountAmount").intValue())
                        .cancelAmount(cancelsNode.get("cancelAmount").longValue())
                        .taxFreeAmount(cancelsNode.get("taxFreeAmount").longValue())
                        .refundableAmount(cancelsNode.get("refundableAmount").longValue())
                        .cancelStatus(PaymentStatus.of(cancelsNode.get("cancelStatus").textValue()))
                        .receiptKey(cancelsNode.get("receiptKey").isNull() ? null : cancelsNode.get("receiptKey").textValue())
                        .cancelRequestId(cancelsNode.get("cancelRequestId").isNull() ? null : cancelsNode.get("cancelRequestId").textValue())
                        .build();
                break;
            }
        }

        // 저장할 데이터
        PaymentCancel paymentCancel = PaymentCancel.builder()
                .version(version)
                .paymentKey(paymentKey)
                .type(type)
                .orderId(orderId)
                .mId(mId)
                .currency(currency)
                .method(method)
                .totalAmount(totalAmount)
                .status(status)
                .requestedAt(requestedAt)
                .card(card)
                .easyPay(easyPay)
                .cancels(cancels)
                .build();

        paymentCancelRepository.save(paymentCancel);
    }

//    public void getPayments(Long userId, Long storeId, Long revervationId, String paymentKey, String orderId, String amount) {
//        ResponseEntity<String> responseEntity = tossPaymentsService.getPaymentDetails(paymentKey , orderId , amount);
//
//        if (responseEntity.getStatusCode().is2xxSuccessful()) {
//            // 응답 본문 가져오기
//            String responseBody = responseEntity.getBody();
//
//            // JSON 파싱
//            ObjectMapper objectMapper = new ObjectMapper();
//            try {
//                // JSON 문자열을 Map으로 변환
//                Map<String, Object> paymentDetails = objectMapper.readValue(responseBody, Map.class);
//
//                // 필요한 데이터 접근
//                String status = (String) paymentDetails.get("status");
//
//
//            } catch (Exception e) {
//                // JSON 파싱 중 오류 처리
//                e.printStackTrace();
//            }
//        } else {
//            throw new PaymentNotFoundException(ResponseCode.PAYMENT_NOT_FOUND);
//        }
//    }
}
