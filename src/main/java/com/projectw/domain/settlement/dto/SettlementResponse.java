package com.projectw.domain.settlement.dto;

import com.projectw.domain.payment.enums.PaymentMethod;
import com.projectw.domain.payment.enums.Status;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public sealed interface SettlementResponse permits SettlementResponse.Log, SettlementResponse.Summary {
    record Log(
            Long id,
            String orderId,
            PaymentMethod method,
            Long amount,
            OffsetDateTime approvedAt,
            LocalDate soldDate,
            LocalDate paidOutDate,
            Status status
    ) implements SettlementResponse {}

    record Summary(
            String summaryDate,
            Long totalAmount,
            Long totalFee,
            Long totalTransactions
    ) implements SettlementResponse {}
}
