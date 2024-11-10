package com.projectw.domain.settlement.repository;

import com.projectw.domain.settlement.dto.SettlementRequest;
import com.projectw.domain.settlement.dto.SettlementResponse;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static com.projectw.domain.settlement.entity.QSettlementSummary.settlementSummary;

@Slf4j
@RequiredArgsConstructor
public class SettlementSummaryDslRepositoryImpl implements SettlementSummaryDslRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<SettlementResponse.Summary> getSettlementSummary(Long userId, Long storeId, SettlementRequest.Summary summary) {

        return queryFactory
                .select(Projections.constructor(SettlementResponse.Summary.class ,
                        settlementSummary.summaryDate,
                        settlementSummary.totalAmount,
                        settlementSummary.totalFee,
                        settlementSummary.totalTransactions))
                .from(settlementSummary)
                .where(
                        settlementSummary.type.eq(summary.summaryType()),
                        settlementSummary.user.id.eq(userId),
                        settlementSummary.store.id.eq(storeId),
                        startDtEquals(summary.startDt()),
                        endDtEquals(summary.endDt())
                )
                .fetch();
    }

    private BooleanExpression startDtEquals(String startDt) {
        return startDt != null ? settlementSummary.summaryDate.goe(startDt) : null;
    }

    private BooleanExpression endDtEquals(String endDt) {
        return endDt != null ? settlementSummary.summaryDate.loe(endDt) : null;
    }
}
