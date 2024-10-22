package com.projectw.domain.waiting.entity;

import com.projectw.common.entity.Timestamped;
import com.projectw.domain.user.entity.User;
import jakarta.persistence.*;

@Entity
@Table(name = "waiting")
public class Waiting extends Timestamped {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String waitDt;

    private int waitNo;

    private int numberPeople;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "store_id", nullable = false)
//    private Store store;

}
