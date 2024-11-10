package com.projectw.domain.settlement.repository;

import com.projectw.domain.settlement.entity.SettlementSummary;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettlementSummaryRepository extends JpaRepository<SettlementSummary , Long> , SettlementSummaryDslRepository {
}
