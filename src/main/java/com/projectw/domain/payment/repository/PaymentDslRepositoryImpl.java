package com.projectw.domain.payment.repository;

import com.projectw.domain.payment.dto.PaymentRequest;
import com.projectw.domain.payment.dto.PaymentResponse;
import com.projectw.domain.payment.enums.Status;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Wildcard;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static com.projectw.domain.payment.entity.QPayment.payment;
import static com.projectw.domain.payment.entity.QPaymentCancel.paymentCancel;
import static com.projectw.domain.payment.entity.QPaymentSuccess.paymentSuccess;

@Slf4j
@RequiredArgsConstructor
public class PaymentDslRepositoryImpl implements PaymentDslRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<PaymentResponse.Payment> getPayments(Long userId, PaymentRequest.Payment pmt, Pageable pageable) {
        List<PaymentResponse.Payment> results = queryFactory
                .select(Projections.constructor(PaymentResponse.Payment.class ,
                        paymentCancel.paymentKey.coalesce(paymentSuccess.paymentKey),
                        payment.orderId,
                        payment.orderName,
                        paymentCancel.totalAmount.coalesce(paymentSuccess.totalAmount),
                        payment.status,
                        paymentCancel.method.coalesce(paymentSuccess.method),
                        paymentCancel.requestedAt.coalesce(paymentSuccess.requestedAt)
                        ))
                .from(payment)
                .leftJoin(paymentSuccess).on(paymentSuccess.orderId.eq(payment.orderId))
                .leftJoin(paymentCancel).on(paymentCancel.orderId.eq(payment.orderId))
                .where(
                        payment.user.id.eq(userId),
                        statusEquals(pmt.status()),
                        startDtEquals(pmt.startDt()),
                        endDtEquals(pmt.endDt())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long totalCount = queryFactory
                .select(Wildcard.count)
                .from(payment)
                .leftJoin(paymentSuccess).on(paymentSuccess.orderId.eq(payment.orderId))
                .leftJoin(paymentCancel).on(paymentCancel.orderId.eq(payment.orderId))
                .where(
                        payment.user.id.eq(userId),
                        statusEquals(pmt.status()),
                        startDtEquals(pmt.startDt()),
                        endDtEquals(pmt.endDt())
                ).fetchOne();

        return new PageImpl<>(results, pageable, totalCount);
    }

    private BooleanExpression statusEquals(Status status) {
        return status != null ? payment.status.eq(status) : null;
    }

    // 시작 날짜 변환
    private BooleanExpression startDtEquals(LocalDate startDt) {
        if (startDt == null) return null;
        OffsetDateTime startDateTime = startDt.atTime(LocalTime.MIN).atOffset(ZoneOffset.UTC);
        return paymentCancel.requestedAt.coalesce(paymentSuccess.requestedAt).goe(startDateTime);
    }

    // 종료 날짜 변환
    private BooleanExpression endDtEquals(LocalDate endDt) {
        if (endDt == null) return null;
        OffsetDateTime endDateTime = endDt.atTime(LocalTime.MAX).atOffset(ZoneOffset.UTC);
        return paymentCancel.requestedAt.coalesce(paymentSuccess.requestedAt).loe(endDateTime);
    }

}
