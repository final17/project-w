package com.projectw.domain.store.entity;

import com.projectw.common.entity.Timestamped;
import com.projectw.domain.reservation.entity.Reservation;
import com.projectw.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Getter
@Entity
@NoArgsConstructor
public class Store extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String image;

    @Column(nullable = false)
    private String title;

    private String description;

    @Column(nullable = false)
    private LocalTime open;

    @Column(nullable = false)
    private LocalTime close;

    private Boolean isNextDay = false;

    @Column(nullable = false)
    private Long reservationTableCount;

    @Column(nullable = false)
    private Long tableCount;

    @Column(nullable = false)
    private String phoneNumber;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private LocalTime lastOrder;

    @Column(nullable = false)
    private LocalTime turnover;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private User user;

    @OneToMany(mappedBy = "store")
    private List<Reservation> reservations = new ArrayList<>();

    @Builder
    public Store(String image, String title, String description, LocalTime open, LocalTime close, Boolean isNextDay, Long tableCount, String phoneNumber, String address, LocalTime lastOrder, LocalTime turnover, User user, List<Reservation> reservations) {
        this.image = image;
        this.title = title;
        this.description = description;
        this.open = open;
        this.close = close;
        this.isNextDay = isNextDay;
        this.tableCount = tableCount;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.lastOrder = lastOrder;
        this.turnover = turnover;
        this.user = user;
        this.reservations = reservations;
    }
}
