package com.projectw.domain.store.entity;

import com.projectw.common.entity.Timestamped;
import com.projectw.domain.category.DistrictCategory;
import com.projectw.domain.category.HierarchicalCategory;
import com.projectw.domain.category.HierarchicalCategoryUtils;
import com.projectw.domain.menu.entity.Menu;
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

    private String districtCategory;

    private String image;

    @Column(nullable = false)
    private String title;

    private String description;

    private Double latitude;
    private Double longitude;

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

    @OneToMany(mappedBy = "store", fetch = FetchType.LAZY)
    private List<Reservation> reservations = new ArrayList<>();

    public Long getOwnerId() {
        return user.getId();
    }

    @OneToMany(mappedBy = "store", fetch = FetchType.LAZY)
    private List<Menu> menus = new ArrayList<>();

    @Builder
    public Store(String image, String districtCategoryCode, String title, String description, LocalTime openTime, LocalTime closeTime, Boolean isNextDay, Long reservationTableCount, Long tableCount, String phoneNumber, String address, LocalTime lastOrder, LocalTime turnover, User user, List<Reservation> reservations, Long deposit, Double latitude, Double longitude) {
        this.image = image;
        this.title = title;
        this.districtCategory = districtCategoryCode;
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
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Store putStore(StoreRequestDto storeRequestDto) {
        HierarchicalCategory category = HierarchicalCategoryUtils.codeToCategory(DistrictCategory.class, storeRequestDto.getDistrictCategoryCode());
        this.image = null;
        this.title = storeRequestDto.getTitle();
        this.districtCategory = category.getPath();
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
        this.latitude = storeRequestDto.getLatitude();
        this.longitude = storeRequestDto.getLongitude();
        return this;
    }

    public void deleteStore() {
        this.isDeleted = true;
    }
}
