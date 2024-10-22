package com.projectw.domain.reservation.entity;

import com.projectw.common.entity.Timestamped;
import com.projectw.domain.store.entity.Store;
import com.projectw.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Reservation extends Timestamped {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


//    @Column(nullable = false)
//    @Enumerated(value = EnumType.STRING)
//    private ReservationStatus status;  // 상태값 웨이팅 , 예약에 같은 기준으로 묶을수 있는거 명칭

    @Column(nullable = false)
    private String type; // 예약 , 웨이팅

    private boolean menuYN; // 메뉴 선택 여부

    @Column(nullable = false)
    private String reservationDate;

    @Column(nullable = false)
    private String reservationTime;

    @Column(nullable = false)
    private int reservationNo;

    @Column(nullable = false)
    private int numberPeople;   // 예약인원 , 입장인원


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;
}
