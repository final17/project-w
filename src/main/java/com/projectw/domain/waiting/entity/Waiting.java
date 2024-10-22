package com.projectw.domain.waiting.entity;

import com.projectw.common.entity.Timestamped;
import com.projectw.domain.user.entity.User;
import com.projectw.domain.waiting.enums.WaitingStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "waiting")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Waiting extends Timestamped {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private WaitingStatus status;

    @Column(nullable = false)
    private String waitDt;

    @Column(nullable = false)
    private int waitNo;

    @Column(nullable = false)
    private int numberPeople;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "store_id", nullable = false)
//    private Store store;

}
