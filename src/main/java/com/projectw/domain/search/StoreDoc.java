package com.projectw.domain.search;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.projectw.domain.menu.entity.Menu;
import com.projectw.domain.store.entity.Store;
import com.projectw.domain.user.entity.User;
import lombok.*;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class StoreDoc {
    private Integer id;

    private String address;

    @JsonProperty("close_time")
    private String closeTime;

    private String description;

    private String image;

    @JsonProperty("is_deleted")
    private Boolean isDeleted;

    @JsonProperty("is_next_day")
    private Boolean isNextDay;

    @JsonProperty("last_order")
    private String lastOrder;

    @JsonProperty("open_time")
    private String openTime;

    @JsonProperty("phone_number")
    private String phoneNumber;

    @JsonProperty("reservation_table_count")
    private Integer reservationTableCount;

    @JsonProperty("table_count")
    private Integer tableCount;

    private String title;

    private String turnover;

    private String menu;

    @JsonProperty("user_id")
    private Integer userId;

    private Integer deposit;

    private Double latitude;  // Float -> Double로 변경

    private Double longitude; // Float -> Double로 변경

    @JsonProperty("district_category")
    private String districtCategory;

    @Builder.Default
    private Long storeLikeCount = 0L;


    public static StoreDoc of(Store store, User user, List<Menu> menus) {
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        String menu = String.join(",", menus.stream().map(Menu::getName).toList());
        return StoreDoc.builder()
                .id(store.getId().intValue())
                .address(store.getAddress())
                .closeTime(store.getCloseTime().format(timeFormatter))
                .description(store.getDescription())
                .image(store.getImage())
                .isDeleted(store.getIsDeleted())
                .isNextDay(store.getIsNextDay())
                .lastOrder(store.getLastOrder().format(timeFormatter))
                .openTime(store.getOpenTime().format(timeFormatter))
                .phoneNumber(store.getPhoneNumber())
                .reservationTableCount(store.getReservationTableCount().intValue())
                .tableCount(store.getTableCount().intValue())
                .title(store.getTitle())
                .turnover(store.getTurnover().format(timeFormatter))
                .menu(menu) // `store.getMenu()`가 Menu 객체를 반환한다고 가정
                .userId(user.getId().intValue()) // `store.getUser()`가 User 객체를 반환한다고 가정
                .deposit(store.getDeposit().intValue())
                .latitude(store.getLatitude())
                .longitude(store.getLongitude())
                .districtCategory(store.getDistrictCategory().getPath())
                .build();
    }

    public void updateStoreLikeCount(long storeLikeCount) {
        this.storeLikeCount = storeLikeCount;
    }
}
