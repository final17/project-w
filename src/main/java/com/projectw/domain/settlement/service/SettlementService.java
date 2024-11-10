package com.projectw.domain.settlement.service;

import com.projectw.domain.settlement.dto.SettlementRequest;
import com.projectw.domain.settlement.dto.SettlementResponse;
import com.projectw.domain.settlement.repository.SettlementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class SettlementService {
    private final SettlementRepository settlementRepository;

    public Page<SettlementResponse.Log> getSettlementLog(Long userId , Long storeId , SettlementRequest.Log log) {
        return settlementRepository.getSettlementLog(userId , storeId , log);
    }
}
