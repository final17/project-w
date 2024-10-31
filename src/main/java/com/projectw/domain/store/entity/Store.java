package com.projectw.domain.store.entity;

import com.projectw.common.entity.Timestamped;
import com.projectw.common.enums.Category;
import com.projectw.domain.reservation.entity.Reservation;
import com.projectw.domain.store.dto.request.StoreRequestDto;
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

    @Enumerated(EnumType.STRING)
    private Category category;

    private String image;

    @Column(nullable = false)
    private String title;

    private String description;

    @Column(nullable = false)
    private LocalTime openTime;

    @Column(nullable = false)
    private LocalTime closeTime;

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

    @Column(nullable = false)
    private Long deposit;

    @Column(nullable = false)
    private Boolean isDeleted = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private User user;

    @OneToMany(mappedBy = "store")
    private List<Reservation> reservations = new ArrayList<>();

    @Builder
    public Store(String image, String title, String description, LocalTime openTime, LocalTime closeTime, Boolean isNextDay, Long reservationTableCount, Long tableCount, String phoneNumber, String address, LocalTime lastOrder, LocalTime turnover, User user, List<Reservation> reservations, Long deposit) {
        this.image = image;
        this.title = title;
        this.description = description;
        this.openTime = openTime;
        this.closeTime = closeTime;
        this.isNextDay = isNextDay;
        this.reservationTableCount = reservationTableCount;
        this.tableCount = tableCount;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.lastOrder = lastOrder;
        this.turnover = turnover;
        this.user = user;
        this.reservations = reservations;
        this.deposit = deposit;
    }

    public Store putStore(StoreRequestDto storeRequestDto) {
        this.image = null;
        this.title = storeRequestDto.getTitle();
        this.description = storeRequestDto.getDescription();
        this.openTime = storeRequestDto.getOpenTime();
        this.lastOrder = storeRequestDto.getLastOrder();
        this.closeTime = storeRequestDto.getCloseTime();
        this.turnover = storeRequestDto.getTurnover();
        this.reservationTableCount = storeRequestDto.getReservationTableCount();
        this.tableCount = storeRequestDto.getTableCount();
        this.address = storeRequestDto.getAddress();
        this.isNextDay = openTime.isAfter(lastOrder);
        this.deposit = storeRequestDto.getDeposit();
        return this;
    }

    public void deleteStore() {
        this.isDeleted = true;
    }
}
