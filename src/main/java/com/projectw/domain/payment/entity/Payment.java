package com.projectw.domain.payment.entity;

import com.projectw.domain.payment.enums.PaymentMethod;
import com.projectw.domain.payment.enums.PaymentStatus;
import com.projectw.domain.payment.enums.PaymentType;
import com.projectw.domain.store.entity.Store;
import com.projectw.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Entity
@Table(name = "payment")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String version; // Payment 객체

    private String paymentKey; // 결제의 키 값

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private PaymentType type; // 결제 타입 정보 : NORMAL , BILLING , BRANDPAY

    @Column(nullable = false , unique = true)
    private String orderId; // 직접 만들어서 리턴해야함!

    private String orderName; // XX식당 X명 예약

    private String mid; // 상점아이디(MID)

    private String currency; // 결제할 때 사용한 통화

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private PaymentMethod method; // 결제수단 : 카드 , 가상계좌 , 간편결제 , 휴대폰 , 계좌이체 , 문화상품권

    @Column(nullable = false)
    private Long totalAmount; // 총 결제 금액

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private PaymentStatus status; // 결제처리상태 : READY , IN_PROGRESS , WAITING_FOR_DEPOSIT , DONE , CANCELED , ABORTED , EXPIRED

    private LocalDate requestedAt; // 결제가 일어난 날짜의 시간 정보

    @Embedded
    private Card card; // 카드 정보

    @Embedded
    private EasyPay easyPay; // 간편결제 정보

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Builder
    public Payment(String version,
                   String paymentKey,
                   PaymentType type,
                   String orderId,
                   String orderName,
                   String mid,
                   String currency,
                   PaymentMethod method,
                   Long totalAmount,
                   PaymentStatus status,
                   LocalDate requestedAt,
                   Card card,
                   EasyPay easyPay,
                   User user,
                   Store store) {
        this.version = version;
        this.paymentKey = paymentKey;
        this.type = type;
        this.orderId = orderId;
        this.orderName = orderName;
        this.mid = mid;
        this.currency = currency;
        this.method = method;
        this.totalAmount = totalAmount;
        this.status = status;
        this.requestedAt = requestedAt;
        this.card = card;
        this.easyPay = easyPay;
        this.user = user;
        this.store = store;
    }

}
