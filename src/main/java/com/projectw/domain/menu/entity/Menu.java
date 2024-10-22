package com.projectw.domain.menu.entity;

import com.projectw.domain.store.entity.Store;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Menu {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name; // 메뉴 이름
    private int price; // 메뉴 가격
    private String allergies; // 알레르기 정보
    private String menuImageUrl; // 메뉴 이미지 URL

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private Store store;
}
