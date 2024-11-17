package com.projectw.domain.settlement.entity;

import com.projectw.common.entity.Timestamped;
import com.projectw.domain.payment.enums.PaymentMethod;
import com.projectw.domain.payment.enums.Status;
import com.projectw.domain.store.entity.Store;
import com.projectw.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Getter
@Entity
@Table(name = "settlement")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Settlement extends Timestamped {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String mId;     // 상점아이디(MID)

    @Column(nullable = false)
    private String paymentKey; // 결제의 키 값

    @Column(nullable = false , unique = true)
    private String orderId;     // 주문번호

    @Column(nullable = false)
    private String currency;    // 결제할때 사용한 통화

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private PaymentMethod method; // 결제수단 : 카드 , 가상계좌 , 간편결제 , 휴대폰 , 계좌이체 , 문화상품권

    @Column(nullable = false)
    private Long amount; // 결제 금액

    @Column(nullable = false)
    private OffsetDateTime approvedAt; // 결제 승인 시간

    @Column(nullable = false)
    private LocalDate soldDate; // 지급 금액의 정산 기준이 되는 정산 매출일 + 3일(주말일 경우 주말을 계산해서 만들기)

    @Column(nullable = false)
    private LocalDate paidOutDate;  // 지급 금액을 상점에 지급할 정산 지급일 + 3일(주말일 경우 주말을 계산해서 만들기)

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private Status status;  // 대기 , 취소 , 완료 , 실패

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Builder
    public Settlement(String mId ,
                      String paymentKey ,
                      String orderId ,
                      String currency ,
                      PaymentMethod method ,
                      Long amount ,
                      OffsetDateTime approvedAt ,
                      LocalDate soldDate ,
                      LocalDate paidOutDate ,
                      Status status ,
                      User user ,
                      Store store ) {
        this.mId = mId;
        this.paymentKey = paymentKey;
        this.orderId = orderId;
        this.currency = currency;
        this.method = method;
        this.amount = amount;
        this.approvedAt = approvedAt;
        this.soldDate = soldDate;
        this.paidOutDate = paidOutDate;
        this.status = status;
        this.user = user;
        this.store = store;
    }

}
