package com.projectw.domain.settlement.entity;

import com.projectw.common.entity.Timestamped;
import com.projectw.domain.settlement.enums.SummaryType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Entity
@Table(name = "settlement_summary")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SettlementSummary extends Timestamped {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate summaryDate;  // 집계 기준 날짜

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private SummaryType type;

    @Column(nullable = false)
    private Long totalAmount; // 집계된 총 결제 금액

    @Column(nullable = false)
    private Long totalFee; // 집계된 총 수수료

    @Column(nullable = false)
    private Long totalTransactions; // 집계된 총 거래수

    public SettlementSummary(LocalDate summaryDate , SummaryType type , Long totalAmount , Long totalFee , Long totalTransactions) {
        this.summaryDate = summaryDate;
        this.type = type;
        this.totalAmount = totalAmount;
        this.totalFee = totalFee;
        this.totalTransactions = totalTransactions;
    }
}
