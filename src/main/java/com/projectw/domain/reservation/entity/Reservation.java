package com.projectw.domain.reservation.entity;

import com.projectw.common.entity.Timestamped;
import com.projectw.common.enums.ReservationStatus;
import com.projectw.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Reservation extends Timestamped {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 음식점 id

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private User user;

    @Column(nullable = false)
    private Long reservationNumber;

    @Column(nullable = false)
    private Integer numberOfGuests;

    @Column(nullable = false)
    private LocalDate reservationDate;

    @Column(nullable = false)
    private LocalTime reservationTime;

    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    @Builder
    public Reservation(/*Store store,*/ User user, Long reservationNumber, Integer numberOfGuests, LocalDate reservationDate, LocalTime reservationTime ) {
        this.user = user;
        this.reservationNumber = reservationNumber;
        this.numberOfGuests = numberOfGuests;
        this.reservationDate = reservationDate;
        this.reservationTime = reservationTime;
        this.status = ReservationStatus.WAITING;
    }
}
