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
import java.time.OffsetDateTime;
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
                        paymentSuccess.paymentKey,
                        payment.orderId,
                        payment.orderName,
                        paymentCancel.totalAmount.coalesce(paymentSuccess.totalAmount),
                        payment.status,
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

    private BooleanExpression startDtEquals(LocalDate startDt) {
        return startDt != null ? paymentCancel.requestedAt.coalesce(paymentSuccess.requestedAt).goe(OffsetDateTime.from(startDt)) : null;
    }

    private BooleanExpression endDtEquals(LocalDate endDt) {
        return endDt != null ? paymentCancel.requestedAt.coalesce(paymentSuccess.requestedAt).loe(OffsetDateTime.from(endDt)) : null;
    }

}
