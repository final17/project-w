package com.projectw.domain.settlement.repository;

import com.projectw.domain.settlement.entity.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettlementRepository extends JpaRepository<Settlement, Long> , SettlementDslRepository {
}
