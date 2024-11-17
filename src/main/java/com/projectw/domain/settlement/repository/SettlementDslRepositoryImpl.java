package com.projectw.domain.settlement.repository;

import com.projectw.domain.settlement.dto.SettlementRequest;
import com.projectw.domain.settlement.dto.SettlementResponse;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Wildcard;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import static com.projectw.domain.settlement.entity.QSettlement.settlement;
import static com.projectw.domain.settlement.entity.QSettlementFees.settlementFees;

@Slf4j
@RequiredArgsConstructor
public class SettlementDslRepositoryImpl implements SettlementDslRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<SettlementResponse.Log> getSettlementLog(Long userId, Long storeId, SettlementRequest.Log log) {
        Pageable pageable = PageRequest.of(log.page() - 1, log.size());

        List<SettlementResponse.Log> results = queryFactory
                .select(Projections.constructor(SettlementResponse.Log.class ,
                        settlement.id,
                        settlement.orderId,
                        settlement.method,
                        settlement.amount,
                        settlement.approvedAt,
                        settlement.soldDate,
                        settlement.paidOutDate,
                        settlement.status))
                .from(settlement)
                .innerJoin(settlementFees).on(settlementFees.settlement.id.eq(settlement.id))
                .where(
                        startDtEquals(log.startDt()),
                        endDtEquals(log.endDt())
                )
                .groupBy(settlement.orderId)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long totalCount = queryFactory
                .select(Wildcard.count)
                .from(settlement)
                .where(
                        startDtEquals(log.startDt()),
                        endDtEquals(log.endDt())
                ).fetchOne();


        return new PageImpl<>(results, pageable, totalCount);
    }

    private BooleanExpression startDtEquals(LocalDate startDt) {
        return startDt != null ? settlement.approvedAt.goe(OffsetDateTime.from(startDt)) : null;
    }

    private BooleanExpression endDtEquals(LocalDate endDt) {
        return endDt != null ? settlement.approvedAt.loe(OffsetDateTime.from(endDt)) : null;
    }
}
