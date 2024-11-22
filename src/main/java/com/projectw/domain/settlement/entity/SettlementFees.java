package com.projectw.domain.settlement.entity;

import com.projectw.common.entity.Timestamped;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "settlement_fees")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SettlementFees extends Timestamped {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "settlement_id", nullable = false)
    private Settlement settlement;

    private String type;        // 결제 수수료의 상세정보
    private Long supplyAmount;  // 결제 수수료의 공급가액
}

// 웹브라우저에서 결제할때랑
// 앱에서 결제할때가 수수료가 다른 경우도 있음
// 현업에서 정산할때
// 수수료를 관리하는 DB가 있고 정산 수수료 테이블쪽에서 외래키로 가져와서 이 수수료가 어떤 수수료인지 알수 있도록