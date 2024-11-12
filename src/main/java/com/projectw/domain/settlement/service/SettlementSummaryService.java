package com.projectw.domain.settlement.service;

import com.projectw.domain.settlement.dto.SettlementRequest;
import com.projectw.domain.settlement.dto.SettlementResponse;
import com.projectw.domain.settlement.repository.SettlementSummaryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class SettlementSummaryService {
    private final SettlementSummaryRepository settlementSummaryRepository;

    public List<SettlementResponse.Summary> getSettlementSummary(Long storeId, SettlementRequest.Summary summary) {
        return settlementSummaryRepository.getSettlementSummary(storeId, summary);
    }
}
