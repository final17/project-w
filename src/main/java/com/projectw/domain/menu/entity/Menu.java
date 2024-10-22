package com.projectw.domain.menu.entity;


import com.projectw.domain.store.entity.Store;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
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

    public Menu(String name, int price, String allergies, String menuImageUrl, Store store) {
        this.name = name;
        this.price = price;
        this.allergies = allergies;
        this.menuImageUrl = menuImageUrl;
        this.store = store;
    }

    // 메뉴 업데이트 메서드
    public void updateMenu(String name, int price, String allergies, String menuImageUrl) {
        this.name = name;
        this.price = price;
        this.allergies = allergies;
        if (menuImageUrl != null) {
            this.menuImageUrl = menuImageUrl;
        }
    }
}
