package com.projectw.domain.settlement.dto;

import com.projectw.domain.settlement.enums.SummaryType;

import java.time.LocalDate;

public sealed interface SettlementRequest permits SettlementRequest.Log , SettlementRequest.Summary {
    record Log(
            LocalDate startDt,
            LocalDate endDt,
            Integer page,
            Integer size
    ) implements SettlementRequest {}

    record Summary(
            SummaryType summaryType,
            String startDt,
            String endDt
    ) implements SettlementRequest {}
}
