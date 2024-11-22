package com.projectw.domain.waiting.entity;

import com.projectw.common.entity.Timestamped;
import com.projectw.domain.store.entity.Store;
import com.projectw.domain.user.entity.User;
import com.projectw.domain.waiting.enums.WaitingStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WaitingHistory extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private WaitingStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    private Store store;

    @Column(name = "registered_at")
    private LocalDateTime registeredAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "canceled_at")
    private LocalDateTime canceledAt;

    @Column(name = "waiting_time")
    private long waitingTime; // 분단위

    public WaitingHistory(User user, Store store) {
        this.user = user;
        this.store = store;
        this.status = WaitingStatus.REGISTERED;
        registeredAt = LocalDateTime.now();
    }

    public void setComplete() {
        this.status = WaitingStatus.COMPLETED;
        completedAt = LocalDateTime.now();
        waitingTime = Duration.between(registeredAt, completedAt).toMinutes();
    }

    public void setCanceled() {
        this.status = WaitingStatus.CANCELED;
        canceledAt = LocalDateTime.now();
        waitingTime = Duration.between(registeredAt, canceledAt).toMinutes();
    }
}
