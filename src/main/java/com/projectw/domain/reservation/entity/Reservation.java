package com.projectw.domain.reservation.entity;

import com.projectw.common.entity.Timestamped;
import com.projectw.domain.reservation.enums.PaymentStatus;
import com.projectw.domain.reservation.enums.ReservationStatus;
import com.projectw.domain.reservation.enums.ReservationType;
import com.projectw.domain.store.entity.Store;
import com.projectw.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Getter
@Table(name = "reservation")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Reservation extends Timestamped {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private ReservationStatus status;  // 상태값 웨이팅 , 예약에 같은 기준으로 묶을수 있는거 명칭

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private ReservationType type; // 예약 , 웨이팅

    private boolean menuYN; // 메뉴 선택 여부

    @Column(nullable = false)
    private LocalDate reservationDate;

    @Column(nullable = false)
    private LocalTime reservationTime;

    @Column(nullable = false)
    private Long reservationNo;

    @Column(nullable = false)
    private Long numberPeople;   // 예약인원 , 입장인원

    @Column(nullable = false)
    private PaymentStatus paymentStatus;

    @Column(nullable = false)
    private int paymentAmt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Builder
    public Reservation(ReservationStatus status , ReservationType type , boolean menuYN , LocalDate reservationDate , LocalTime reservationTime , Long reservationNo , Long numberPeople , PaymentStatus paymentStatus , int paymentAmt , User user , Store store) {
        this.status = status;
        this.type = type;
        this.menuYN = menuYN;
        this.reservationDate = reservationDate;
        this.reservationTime = reservationTime;
        this.reservationNo = reservationNo;
        this.numberPeople = numberPeople;
        this.paymentStatus = paymentStatus;
        this.paymentAmt = paymentAmt;
        this.user = user;
        this.store = store;
    }

    public void updateStatus(ReservationStatus status) {
        this.status = status;
    }
}
