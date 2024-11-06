package com.projectw.domain.search;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    private Double turnover;  // Float -> Double로 변경

    @JsonProperty("menu_id")
    private Integer menuId;

    @JsonProperty("user_id")
    private Integer userId;

    private Integer deposit;

    private Double latitude;  // Float -> Double로 변경

    private Double longitude; // Float -> Double로 변경

    @JsonProperty("district_category")
    private String districtCategory;
}
