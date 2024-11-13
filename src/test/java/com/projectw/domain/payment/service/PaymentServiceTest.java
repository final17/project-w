package com.projectw.domain.payment.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.projectw.common.enums.ResponseCode;
import com.projectw.common.enums.UserRole;
import com.projectw.common.exceptions.NotFoundException;
import com.projectw.common.exceptions.PaymentNotFoundException;
import com.projectw.common.resttemplate.TossPaymentsService;
import com.projectw.domain.payment.dto.PaymentRequest;
import com.projectw.domain.payment.dto.PaymentResponse;
import com.projectw.domain.payment.entity.Payment;
import com.projectw.domain.payment.entity.PaymentSuccess;
import com.projectw.domain.payment.enums.PaymentMethod;
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
import com.projectw.security.AuthUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.view.RedirectView;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {

    @Mock
    private PaymentSuccessRepository paymentSuccessRepository;
    @Mock
    private PaymentCancelRepository paymentCancelRepository;
    @Mock
    private PaymentFailRepository paymentFailRepository;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private TossPaymentsService tossPaymentsService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private StoreRepository storeRepository;
    @Mock
    private ReservationCheckService reservationCheckService;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private PaymentService paymentService;

    private final String PREFIX_ORDER_ID = "ORDER-";

    private PaymentRequest.Prepare prepare;
    private AuthUser authUser;
    private PaymentRequest.Susscess susscess;
    private String successJsonString;
    private String failJsonString;
    private PaymentRequest.Fail fail;
    private String cancelJsonString;

    @BeforeEach
    void setUpBeforeClass() throws Exception {
        LocalDate date = LocalDate.parse("2024-11-11");
        LocalTime time = LocalTime.parse("11:00:00");
        Long storeId = 1L;
        Long amount = 5000L;
        Long numberPeople = 2L;
        String orderName = "중식당";
        prepare = new PaymentRequest.Prepare(date , time , storeId , amount , numberPeople , orderName);
        authUser = new AuthUser(1L , "test@test.com" , UserRole.ROLE_USER);

        String paymentKey = "JLSAID3123FASD";
        String orderId = PREFIX_ORDER_ID + "ASDAS232ASDAS";
        susscess = new PaymentRequest.Susscess(paymentKey , orderId , amount);

        successJsonString = String.format("""
                    {
                      "mId": "tosspayments",
                      "lastTransactionKey": "9C62B18EEF0DE3EB7F4422EB6D14BC6E",
                      "paymentKey": "%s",
                      "orderId": "%s",
                      "orderName": "토스 티셔츠 외 2건",
                      "taxExemptionAmount": 0,
                      "status": "DONE",
                      "requestedAt": "2024-02-13T12:17:57+09:00",
                      "approvedAt": "2024-02-13T12:18:14+09:00",
                      "useEscrow": false,
                      "cultureExpense": false,
                      "card": {
                        "issuerCode": "71",
                        "acquirerCode": "71",
                        "number": "12345678****000*",
                        "installmentPlanMonths": 0,
                        "isInterestFree": false,
                        "interestPayer": null,
                        "approveNo": "00000000",
                        "useCardPoint": false,
                        "cardType": "신용",
                        "ownerType": "개인",
                        "acquireStatus": "READY",
                        "receiptUrl": "https://dashboard.tosspayments.com/receipt/redirection?transactionId=tviva20240213121757MvuS8&ref=PX",
                        "amount": 1000
                      },
                      "virtualAccount": null,
                      "transfer": null,
                      "mobilePhone": null,
                      "giftCertificate": null,
                      "cashReceipt": null,
                      "cashReceipts": null,
                      "discount": null,
                      "cancels": null,
                      "secret": null,
                      "type": "NORMAL",
                      "easyPay": {
                        "provider": "토스페이",
                        "amount": 0,
                        "discountAmount": 0
                      },
                      "easyPayAmount": 0,
                      "easyPayDiscountAmount": 0,
                      "country": "KR",
                      "failure": null,
                      "isPartialCancelable": true,
                      "receipt": {
                        "url": "https://dashboard.tosspayments.com/receipt/redirection?transactionId=tviva20240213121757MvuS8&ref=PX"
                      },
                      "checkout": {
                        "url": "https://api.tosspayments.com/v1/payments/5EnNZRJGvaBX7zk2yd8ydw26XvwXkLrx9POLqKQjmAw4b0e1/checkout"
                      },
                      "currency": "KRW",
                      "totalAmount": 1000,
                      "balanceAmount": 1000,
                      "suppliedAmount": 909,
                      "vat": 91,
                      "taxFreeAmount": 0,
                      "method": "카드",
                      "version": "2022-11-16"
                    }
                    """, paymentKey , orderId);


        String failCode = "400";
        String failMessage = "에러";
        failJsonString = String.format("""
                {
                    "code" : "%s",
                    "message" : "%s"
                }
                """ , failCode , failMessage);

        fail = new PaymentRequest.Fail(failMessage , failCode);

        String cancelReason = "단순변심";
        cancelJsonString = String.format("""
                {
                  "mId": "tosspayments",
                  "lastTransactionKey": "090A796806E726BBB929F4A2CA7DB9A7",
                  "paymentKey": "%s",
                  "orderId": "%s",
                  "orderName": "토스 티셔츠 외 2건",
                  "taxExemptionAmount": 0,
                  "status": "CANCELED",
                  "requestedAt": "2024-02-13T12:17:57+09:00",
                  "approvedAt": "2024-02-13T12:18:14+09:00",
                  "useEscrow": false,
                  "cultureExpense": false,
                  "card": {
                    "issuerCode": "71",
                    "acquirerCode": "71",
                    "number": "12345678****000*",
                    "installmentPlanMonths": 0,
                    "isInterestFree": false,
                    "interestPayer": null,
                    "approveNo": "00000000",
                    "useCardPoint": false,
                    "cardType": "신용",
                    "ownerType": "개인",
                    "acquireStatus": "READY",
                    "amount": 1000
                  },
                  "virtualAccount": null,
                  "transfer": null,
                  "mobilePhone": null,
                  "giftCertificate": null,
                  "cashReceipt": null,
                  "cashReceipts": null,
                  "discount": null,
                  "cancels": [
                    {
                      "transactionKey": "090A796806E726BBB929F4A2CA7DB9A7",
                      "cancelReason": "%s",
                      "taxExemptionAmount": 0,
                      "canceledAt": "2024-02-13T12:20:23+09:00",
                      "easyPayDiscountAmount": 0,
                      "receiptKey": null,
                      "cancelAmount": 1000,
                      "taxFreeAmount": 0,
                      "refundableAmount": 0,
                      "cancelStatus": "DONE",
                      "cancelRequestId": null
                    }
                  ],
                  "secret": null,
                  "type": "NORMAL",
                  "easyPay": {
                    "provider": "토스페이",
                    "amount": 0,
                    "discountAmount": 0
                  },
                  "easyPayAmount": 0,
                  "easyPayDiscountAmount": 0,
                  "country": "KR",
                  "failure": null,
                  "isPartialCancelable": true,
                  "receipt": {
                    "url": "https://dashboard.tosspayments.com/receipt/redirection?transactionId=tviva20240213121757MvuS8&ref=PX"
                  },
                  "checkout": {
                    "url": "https://api.tosspayments.com/v1/payments/5EnNZRJGvaBX7zk2yd8ydw26XvwXkLrx9POLqKQjmAw4b0e1/checkout"
                  },
                  "currency": "KRW",
                  "totalAmount": 1000,
                  "balanceAmount": 0,
                  "suppliedAmount": 0,
                  "vat": 0,
                  "taxFreeAmount": 0,
                  "method": "카드",
                  "version": "2022-11-16"
                }
                """
                , paymentKey , orderId , cancelReason);
    }

    @Test
    public void prepare_동작완료() {
        // given
        doNothing().when(reservationCheckService).isReservationDateValid(any() , any());
        User user = User.fromAuthUser(authUser);
        Store store = Store.builder()
                .title("중식당")
                .openTime(LocalTime.parse("11:00:00"))
                .closeTime(LocalTime.parse("12:00:00"))
                .build();
        ReflectionTestUtils.setField(store, "id", 1L);

        given(userRepository.findById(anyLong())).willReturn(Optional.of(user));
        given(storeRepository.findById(anyLong())).willReturn(Optional.of(store));

        doNothing().when(reservationCheckService).validateMenuPresence(anyLong() , anyLong());

        doNothing().when(reservationCheckService).validateMenuPricesEqual(anyLong() , anyLong() , anyLong());

        doNothing().when(reservationCheckService).validateUserAuthorization(any() , any());

        doNothing().when(reservationCheckService).validateReservationTime(any() , any());

        given(reservationCheckService.checkReservationCapacity(any() , any() , any())).willReturn(true);

        String orderId = PREFIX_ORDER_ID + UUID.randomUUID().toString().replaceAll("-" , "");

        Payment savePayment = Payment.builder()
                .orderId(orderId)
                .orderName(prepare.orderName())
                .amount(prepare.amount())
                .build();

        given(paymentRepository.save(any(Payment.class))).willReturn(savePayment);
        doNothing().when(eventPublisher).publishEvent(any(ReservationInsertEvent.class));

        PaymentResponse.Prepare paymentResponse = new PaymentResponse.Prepare(orderId , prepare.orderName(), prepare.amount());

        // when
        PaymentResponse.Prepare result = paymentService.prepare(authUser.getUserId() , prepare);

        // then
        assertEquals(paymentResponse.orderName() , result.orderName());
        assertEquals(paymentResponse.amount() , result.amount());
    }

    @Test
    public void prepare_좌석없음_예외처리() {
        // given
        doNothing().when(reservationCheckService).isReservationDateValid(any() , any());
        User user = User.fromAuthUser(authUser);
        Store store = Store.builder()
                .title("중식당")
                .openTime(LocalTime.parse("11:00:00"))
                .closeTime(LocalTime.parse("12:00:00"))
                .build();
        ReflectionTestUtils.setField(store, "id", 1L);

        given(userRepository.findById(anyLong())).willReturn(Optional.of(user));
        given(storeRepository.findById(anyLong())).willReturn(Optional.of(store));

        doNothing().when(reservationCheckService).validateMenuPresence(anyLong() , anyLong());

        doNothing().when(reservationCheckService).validateMenuPricesEqual(anyLong() , anyLong() , anyLong());

        doNothing().when(reservationCheckService).validateUserAuthorization(any() , any());

        doNothing().when(reservationCheckService).validateReservationTime(any() , any());

        given(reservationCheckService.checkReservationCapacity(any() , any() , any())).willReturn(false);

        // when
        InsufficientSeatsException exception = assertThrows(InsufficientSeatsException.class, () -> paymentService.prepare(authUser.getUserId() , prepare));

        // then
        assertEquals(exception.getMessage() , ResponseCode.INSUFFICIENT_SEAT.getMessage());
    }


    @Test
    public void prepare_가게정보_없음_예외처리() {
        // given
        doNothing().when(reservationCheckService).isReservationDateValid(any() , any());
        User user = User.fromAuthUser(authUser);
        Store store = Store.builder()
                .title("중식당")
                .turnover(LocalTime.parse("00:30:00"))
                .openTime(LocalTime.parse("11:00:00"))
                .closeTime(LocalTime.parse("12:00:00"))
                .build();
        ReflectionTestUtils.setField(store, "id", 1L);

        given(userRepository.findById(anyLong())).willReturn(Optional.of(user));
        given(storeRepository.findById(anyLong())).willReturn(Optional.empty());

        // when
        NotFoundException exception = assertThrows(NotFoundException.class, () -> paymentService.prepare(authUser.getUserId() , prepare));

        // then
        assertEquals(exception.getMessage() , ResponseCode.NOT_FOUND_STORE.getMessage());
    }


    @Test
    public void prepare_유저정보_없음_예외처리() {
        // given
        doNothing().when(reservationCheckService).isReservationDateValid(any() , any());
        given(userRepository.findById(anyLong())).willReturn(Optional.empty());

        // when
        NotFoundException exception = assertThrows(NotFoundException.class, () -> paymentService.prepare(authUser.getUserId() , prepare));

        // then
        assertEquals(exception.getMessage() , ResponseCode.NOT_FOUND_USER.getMessage());
    }

    @Test
    public void success_결제승인성공_정상동작() throws Exception {
        // given
        Payment payment = Payment.builder()
                .orderId(susscess.orderId())
                .amount(susscess.amount())
                .status(Status.PENDING)
                .build();

        given(paymentRepository.findByOrderIdAndStatus(any() , any())).willReturn(Optional.of(payment));

        ResponseEntity<String> response = new ResponseEntity<>(successJsonString, HttpStatus.OK);
        String responseBody = response.getBody();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(responseBody);

        given(tossPaymentsService.confirm(any() , any() , any())).willReturn(response);

        doNothing().when(eventPublisher).publishEvent(any(ReservationPaymentCompEvent.class));

        // when
        RedirectView redirectView = paymentService.success(susscess);

        // then
        assertEquals(String.format("null/payment/success?paymentKey=%s&orderId=%s&amount=%d" , susscess.paymentKey() , susscess.orderId() , susscess.amount()) , redirectView.getUrl());
    }

    @Test
    public void success_결제승인실패_정상동작() throws Exception {
        // given
        Payment payment = Payment.builder()
                .orderId(susscess.orderId())
                .amount(susscess.amount())
                .status(Status.PENDING)
                .build();

        given(paymentRepository.findByOrderIdAndStatus(any() , any())).willReturn(Optional.of(payment));

        ResponseEntity<String> response = new ResponseEntity<>(failJsonString, HttpStatus.BAD_REQUEST);

        given(tossPaymentsService.confirm(any() , any() , any())).willReturn(response);

        String failCode = "400";
        String failMessage = "에러";

        // when
        RedirectView redirectView = paymentService.success(susscess);

        // then
        assertEquals(String.format("null/payment/fail?code=%s&message='%s'" , failCode , failMessage) , redirectView.getUrl());
    }

    @Test
    public void success_결제건_정보없음_예외처리() {
        // given
        given(paymentRepository.findByOrderIdAndStatus(any() , any())).willReturn(Optional.empty());

        // when
        PaymentNotFoundException exception = assertThrows(PaymentNotFoundException.class, () -> paymentService.success(susscess));

        // then
        assertEquals(ResponseCode.PAYMENT_NOT_FOUND.getMessage() , exception.getMessage());
    }

    @Test
    public void fail_정상동작() throws Exception {

        // when
        RedirectView redirectView = paymentService.fail(fail);

        // then
        assertEquals(String.format("null/payment/fail?code=%s&message='%s'" , fail.code() , fail.message()) , redirectView.getUrl());
    }

    @Test
    public void cancel_결제취소성공_정상동작() throws Exception {
        // given
        String orderId = PREFIX_ORDER_ID + "ASDAS232ASDAS";
        String cancelReason = "단순변심";

        PaymentSuccess paymentSuccess = PaymentSuccess.builder().build();
        given(paymentSuccessRepository.findByOrderId(any())).willReturn(Optional.of(paymentSuccess));

        ResponseEntity<String> response = new ResponseEntity<>(cancelJsonString, HttpStatus.OK);
        String responseBody = response.getBody();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(responseBody);

        given(tossPaymentsService.cancel(any() , any())).willReturn(response);

        Method method = PaymentService.class.getDeclaredMethod("cancelPayment", JsonNode.class);
        method.setAccessible(true);  // private 메서드에 접근 가능하도록 설정
        method.invoke(paymentService, jsonNode);

        Payment payment = Payment.builder()
                .orderId(orderId)
                .status(Status.CANCELLED)
                .build();
        given(paymentRepository.findByOrderIdAndStatus(any() , any(Status.class))).willReturn(Optional.of(payment));

        // when
        paymentService.cancel(orderId , cancelReason);

        // then
        assertEquals(Status.CANCELLED , payment.getStatus());
    }

    @Test
    public void cancel_결제취소실패_정상동작() throws Exception {
        // given
        String orderId = PREFIX_ORDER_ID + "ASDAS232ASDAS";
        String cancelReason = "단순변심";

        String failCode = "400";
        String failMessage = "에러";

        PaymentSuccess paymentSuccess = PaymentSuccess.builder().build();
        given(paymentSuccessRepository.findByOrderId(any())).willReturn(Optional.of(paymentSuccess));

        ResponseEntity<String> response = new ResponseEntity<>(failJsonString, HttpStatus.BAD_REQUEST);
        String responseBody = response.getBody();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(responseBody);

        given(tossPaymentsService.cancel(any() , any())).willReturn(response);

        Method method = PaymentService.class.getDeclaredMethod("failPayment", String.class , String.class , String.class);
        method.setAccessible(true);  // private 메서드에 접근 가능하도록 설정
        method.invoke(paymentService, orderId , failCode , failMessage);

        // when
        paymentService.cancel(orderId , cancelReason);

        // then
        assertEquals(failCode , jsonNode.get("code").textValue());
        assertEquals(failMessage , jsonNode.get("message").textValue());
    }

    @Test
    public void cancel_결제정보없음_예외처리() throws Exception {
        // given
        String orderId = PREFIX_ORDER_ID + "ASDAS232ASDAS";
        String cancelReason = "단순변심";

        PaymentSuccess paymentSuccess = PaymentSuccess.builder().build();
        given(paymentSuccessRepository.findByOrderId(any())).willReturn(Optional.of(paymentSuccess));

        ResponseEntity<String> response = new ResponseEntity<>(cancelJsonString, HttpStatus.OK);
        String responseBody = response.getBody();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(responseBody);

        given(tossPaymentsService.cancel(any() , any())).willReturn(response);

        Method method = PaymentService.class.getDeclaredMethod("cancelPayment", JsonNode.class);
        method.setAccessible(true);  // private 메서드에 접근 가능하도록 설정
        method.invoke(paymentService, jsonNode);

        given(paymentRepository.findByOrderIdAndStatus(any() , any(Status.class))).willReturn(Optional.empty());

        // when
        PaymentNotFoundException exception = assertThrows(PaymentNotFoundException.class, () -> paymentService.cancel(orderId , cancelReason));

        // then
        assertEquals(ResponseCode.PAYMENT_NOT_FOUND.getMessage(), exception.getMessage());
    }

    @Test
    public void cancel_결제승인정보없음_예외처리() throws Exception {
        // given
        String orderId = PREFIX_ORDER_ID + "ASDAS232ASDAS";
        String cancelReason = "단순변심";

        given(paymentSuccessRepository.findByOrderId(any())).willReturn(Optional.empty());

        // when
        NotFoundException exception = assertThrows(NotFoundException.class, () -> paymentService.cancel(orderId , cancelReason));

        // then
        assertEquals(ResponseCode.PAYMENT_NOT_FOUND.getMessage(), exception.getMessage());
    }

    @Test
    void getPayments_동작완료() {
        // Given
        Long userId = 1L;
        PaymentRequest.Payment paymentRequest = new PaymentRequest.Payment(
                Status.COMPLETED,
                LocalDate.now().minusDays(30),
                LocalDate.now(),
                null, // page는 null로 설정하여 기본값 확인
                null  // size는 null로 설정하여 기본값 확인
        );

        String orderId = PREFIX_ORDER_ID + "ASDAS232ASDAS";

        PaymentResponse.Payment paymentResponse = new PaymentResponse.Payment(
                "paymentKey1",
                orderId,
                "orderName1",
                10000L,
                Status.COMPLETED,
                PaymentMethod.CARD,
                OffsetDateTime.now()
        );

        List<PaymentResponse.Payment> paymentList = Collections.singletonList(paymentResponse);
        Page<PaymentResponse.Payment> paymentPage = new PageImpl<>(paymentList);

        when(paymentRepository.getPayments(eq(userId), eq(paymentRequest), any(Pageable.class)))
                .thenReturn(paymentPage);

        // When
        Page<PaymentResponse.Payment> result = paymentService.getPayments(userId, paymentRequest);

        // Then
        assertEquals(1, result.getTotalElements());
        assertEquals(paymentResponse.paymentKey(), result.getContent().get(0).paymentKey());
        verify(paymentRepository, times(1)).getPayments(eq(userId), eq(paymentRequest), any(Pageable.class));
    }

    @Test
    void timeoutCancel_동작완료() {
        // Given
        String orderId = PREFIX_ORDER_ID + "ASDAS232ASDAS";
        Payment payment = Payment.builder()
                .orderId(orderId)
                .status(Status.PENDING)
                .build();

        when(paymentRepository.findByOrderIdAndStatus(orderId, Status.PENDING)).thenReturn(Optional.of(payment));

        // When
        paymentService.timeoutCancel(orderId);

        // Then
        assertEquals(Status.CANCELLED, payment.getStatus()); // 상태가 CANCELLED로 변경되었는지 확인
        verify(paymentRepository, times(1)).findByOrderIdAndStatus(orderId, Status.PENDING); // 메서드 호출 확인
    }

    @Test
    void timeoutCancel_결제정보없음_예외처리() {
        // Given
        String orderId = PREFIX_ORDER_ID + "ASDAS232ASDAS";

        when(paymentRepository.findByOrderIdAndStatus(orderId, Status.PENDING)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(PaymentNotFoundException.class, () -> paymentService.timeoutCancel(orderId));
        verify(paymentRepository, times(1)).findByOrderIdAndStatus(orderId, Status.PENDING); // 메서드 호출 확인
    }

}
