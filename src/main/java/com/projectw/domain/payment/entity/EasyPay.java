package com.projectw.domain.payment.entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Embeddable
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EasyPay {
    private String provider;
    private int easyPayAmount;
    private int easyPayDiscountAmount;
}
