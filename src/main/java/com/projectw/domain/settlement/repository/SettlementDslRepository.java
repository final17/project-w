package com.projectw.domain.settlement.repository;

import com.projectw.domain.settlement.dto.SettlementRequest;
import com.projectw.domain.settlement.dto.SettlementResponse;
import org.springframework.data.domain.Page;

public interface SettlementDslRepository {
    Page<SettlementResponse.Log> getSettlementLog(Long userId , Long storeId , SettlementRequest.Log log);
}
