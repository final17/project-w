package com.projectw.domain.settlement.repository;

import com.projectw.domain.settlement.dto.SettlementRequest;
import com.projectw.domain.settlement.dto.SettlementResponse;

import java.util.List;

public interface SettlementSummaryDslRepository {
    List<SettlementResponse.Summary> getSettlementSummary(Long userId, Long storeId, SettlementRequest.Summary summary);
}
