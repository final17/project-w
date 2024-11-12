package com.projectw.domain.store.entity;

import com.projectw.common.entity.Timestamped;
import com.projectw.domain.category.DistrictCategory;
import com.projectw.domain.category.HierarchicalCategoryUtils;
import com.projectw.domain.menu.entity.Menu;
import com.projectw.domain.reservation.entity.Reservation;
import com.projectw.domain.store.dto.StoreRequest;
import com.projectw.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

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
    private DistrictCategory districtCategory;

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

    @Column(nullable = false)
    private Long view;

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
    public Store(String image, DistrictCategory districtCategory, String title, String description, LocalTime openTime, LocalTime closeTime, Boolean isNextDay, Long reservationTableCount, Long tableCount, String phoneNumber, String address, LocalTime lastOrder, LocalTime turnover, User user, List<Reservation> reservations, Long deposit, Double latitude, Double longitude) {
        this.image = image;
        this.title = title;
        this.districtCategory = districtCategory;
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
        this.view = 0L;
    }


    public Store putStore(String imageName, StoreRequest.Create storeRequestDto) {

        DistrictCategory category = null;

        if(StringUtils.hasText(storeRequestDto.districtCategoryCode())) {
            category = HierarchicalCategoryUtils.codeToCategory(DistrictCategory.class, storeRequestDto.districtCategoryCode());
        }

        this.image = imageName;
        this.title = storeRequestDto.title();
        this.districtCategory = category;
        this.description = storeRequestDto.description();
        this.phoneNumber = storeRequestDto.phoneNumber();
        this.openTime = storeRequestDto.openTime();
        this.lastOrder = storeRequestDto.lastOrder();
        this.closeTime = storeRequestDto.closeTime();
        this.turnover = storeRequestDto.turnover();
        this.reservationTableCount = storeRequestDto.reservationTableCount();
        this.tableCount = storeRequestDto.tableCount();
        this.address = storeRequestDto.address();
        this.isNextDay = openTime.isAfter(lastOrder);
        this.deposit = storeRequestDto.deposit();
        this.latitude = storeRequestDto.latitude();
        this.longitude = storeRequestDto.longitude();
        return this;
    }

    public void deleteStore() {
        this.isDeleted = true;
    }

    public void addView() {
        this.view += 1;
    }

    public void updateDistrictCategory(String districtCategoryCode) {
        DistrictCategory category = null;
        if(StringUtils.hasText(districtCategoryCode)) {
            category = HierarchicalCategoryUtils.codeToCategory(DistrictCategory.class, districtCategoryCode);
        }
        this.districtCategory = category;
    }
}
