package com.projectw.domain.payment.entity;

import com.projectw.common.entity.Timestamped;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "payment_fail")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentFail extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String orderId; // 주문 번호

    private String message;

    private String code;

    public PaymentFail(String orderId, String message, String code) {
        this.orderId = orderId;
        this.message = message;
        this.code = code;
    }
}
