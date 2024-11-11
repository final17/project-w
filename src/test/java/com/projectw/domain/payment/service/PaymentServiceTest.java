package com.projectw.domain.payment.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.projectw.common.enums.UserRole;
import com.projectw.common.exceptions.NotFoundException;
import com.projectw.common.resttemplate.TossPaymentsService;
import com.projectw.domain.payment.dto.PaymentRequest;
import com.projectw.domain.payment.dto.PaymentResponse;
import com.projectw.domain.payment.entity.Payment;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.view.RedirectView;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;

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

    private static final String PREFIX_ORDER_ID = "ORDER-";

    private PaymentRequest.Prepare prepare;
    private AuthUser authUser;
    private PaymentRequest.Susscess susscess;
    private String jsonString;

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

        jsonString = String.format("""
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
        assertEquals(exception.getMessage() , "좌석이 부족합니다.");
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
        assertEquals(exception.getMessage() , "해당 가게는 존재하지 않습니다.");
    }


    @Test
    public void prepare_유저정보_없음_예외처리() {
        // given
        doNothing().when(reservationCheckService).isReservationDateValid(any() , any());
        given(userRepository.findById(anyLong())).willReturn(Optional.empty());

        // when
        NotFoundException exception = assertThrows(NotFoundException.class, () -> paymentService.prepare(authUser.getUserId() , prepare));

        // then
        assertEquals(exception.getMessage() , "해당 사용자는 존재하지 않습니다.");
    }

    @Test
    public void success_정상동작() throws Exception {
        // given
        Payment payment = Payment.builder()
                .orderId(susscess.orderId())
                .amount(susscess.amount())
                .status(Status.PENDING)
                .build();

        given(paymentRepository.findByOrderIdAndStatus(any() , any())).willReturn(Optional.of(payment));

        ResponseEntity<String> response = new ResponseEntity<>(jsonString, HttpStatus.OK);
        String responseBody = response.getBody();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(responseBody);

        given(tossPaymentsService.confirm(any() , any() , any())).willReturn(response);

        Method method = PaymentService.class.getDeclaredMethod("confirmPayment", JsonNode.class);
        method.setAccessible(true);  // private 메서드에 접근 가능하도록 설정
        method.invoke(paymentService, jsonNode);

        doNothing().when(eventPublisher).publishEvent(any(ReservationPaymentCompEvent.class));

        // when
        RedirectView redirectView = paymentService.success(susscess);

        // then
        assertEquals(String.format("null/payment/success?paymentKey=%s&orderId=%s&amount=%d" , susscess.paymentKey() , susscess.orderId() , susscess.amount()) , redirectView.getUrl());
    }

}
