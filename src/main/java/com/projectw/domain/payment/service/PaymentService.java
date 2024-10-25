package com.projectw.domain.payment.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.projectw.common.enums.ResponseCode;
import com.projectw.common.exceptions.InvalidRequestException;
import com.projectw.common.exceptions.NotFoundException;
import com.projectw.common.exceptions.UnauthorizedException;
import com.projectw.common.resttemplate.TossPaymentsService;
import com.projectw.domain.payment.dto.PaymentRequest;
import com.projectw.domain.payment.dto.PaymentResponse;
import com.projectw.domain.payment.entity.Payment;
import com.projectw.domain.payment.enums.PaymentMethod;
import com.projectw.domain.payment.enums.PaymentStatus;
import com.projectw.domain.payment.enums.PaymentType;
import com.projectw.domain.payment.repository.PaymentRepository;
import com.projectw.domain.reservation.exception.InvalidReservationTimeException;
import com.projectw.domain.store.entity.Store;
import com.projectw.domain.store.repository.StoreRepository;
import com.projectw.domain.user.entity.User;
import com.projectw.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final TossPaymentsService tossPaymentsService;
    private final UserRepository userRepository;
    private final StoreRepository storeRepository;

    @Transactional
    public PaymentResponse.Prepare prepare(Long userId , PaymentRequest.Prepare prepare) {
        // 현재시간대를 기준으로 예약 가능한 시간 값이 들어왔는지 검증
        LocalDate nowDate = LocalDate.now();
        LocalTime nowTime = LocalTime.now();
        if (prepare.date().isBefore(nowDate)) {
            throw new InvalidReservationTimeException(ResponseCode.INVALID_RESERVATION_TIME);
        } else if(prepare.date().equals(nowDate)) {
            if(prepare.time().isBefore(nowTime)) {
                throw new InvalidReservationTimeException(ResponseCode.INVALID_RESERVATION_TIME);
            }
        }

        // 유저 있는지?
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException(ResponseCode.NOT_FOUND_USER));
        // 식당이 있는지?
        Store store = storeRepository.findById(prepare.storeId()).orElseThrow(() -> new NotFoundException(ResponseCode.NOT_FOUND_STORE));

        // 들어온 금액과 내부의 설정된 금액이 다를때!
        if (!prepare.amount().equals(store.getDeposit())) {
            throw new InvalidRequestException(ResponseCode.INVALID_AMOUNT);
        }

        // 본인 식당에 예약 가능한지?
        if (store.getUser().equals(user)) {
            throw new UnauthorizedException(ResponseCode.UNAUTHORIZED_STORE_RESERVATION);
        }

        // 예약 가능한 시간대인지? 어떻게 처리할지 고민할 것
        int minutes = prepare.time().getHour() * 60 + prepare.time().getMinute();
        int baseMinutes = store.getTurnover().getHour() * 60 + store.getTurnover().getMinute();

        if (minutes % baseMinutes != 0) {
            log.error("예약 불가능한 시간대로 값이 들어왔음!!");
            throw new InvalidReservationTimeException(ResponseCode.INVALID_RESERVATION_TIME);
        }


        String orderId = "ORDER-" + UUID.randomUUID().toString().substring(0, 8);

        Payment payment = Payment.builder()
                .type(PaymentType.NORMAL)
                .orderId(orderId)
                .totalAmount(prepare.amount())
                .method(PaymentMethod.CARD)
                .status(PaymentStatus.READY)
                .user(user)
                .store(store)
                .build();

        paymentRepository.save(payment);

        return new PaymentResponse.Prepare(orderId , prepare.amount());
    }

    @Transactional
    public PaymentResponse.Susscess success(PaymentRequest.Susscess susscess) throws Exception {
        Payment payment = paymentRepository.findByOrderId(susscess.orderId()).orElseThrow(() -> new NotFoundException(ResponseCode.PAYMENT_NOT_FOUND));

        ResponseEntity<String> responseEntity = tossPaymentsService.getPaymentDetails(susscess.paymentKey() , susscess.orderId() , String.valueOf(susscess.amount()));

        ObjectMapper mapper = new ObjectMapper();

        String responseBody = responseEntity.getBody();

        JsonNode jsonNode = mapper.readTree(responseBody);

        log.info("test : {}" , jsonNode);

        if(responseEntity.getStatusCode() == HttpStatus.OK) {

        }
        return null;
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
