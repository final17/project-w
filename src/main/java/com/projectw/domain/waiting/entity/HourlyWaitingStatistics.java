package com.projectw.domain.waiting.entity;

import com.projectw.domain.store.entity.Store;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Builder
public class HourlyWaitingStatistics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private Store store;
    private LocalDate date; // 통계 날짜
    private int hour; // 시간대 (0~23)
    private int totalWaitingCount; // 해당 시간대 총 대기 인원 수
    private int completedCount; // 완료된 대기 건수
    private int canceledCount; // 취소된 대기 건수
    private int maxWaitingTime; // 최대 대기 시간 (분 단위)
    private int minWaitingTime; // 최소 대기 시간 (분 단위)
    private double completedAverageWaitingTime; // 실제로 웨이팅 성공할 때까지 걸린 평균 시간(분)
    private double canceledAverageWaitTime; // 웨이팅 취소할 때까지 걸린 평균 시간(분)

    private LocalDateTime createdAt;
}