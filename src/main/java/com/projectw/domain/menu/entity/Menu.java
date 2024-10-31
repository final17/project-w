package com.projectw.domain.menu.entity;

import com.projectw.domain.allergy.entity.Allergy;
import com.projectw.domain.store.entity.Store;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@NoArgsConstructor
public class Menu {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name; // 메뉴 이름
    private int price; // 메뉴 가격

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private Store store;

    @ManyToMany
    @JoinTable(
            name = "menu_allergies",  // 메뉴-알레르기 중간 테이블
            joinColumns = @JoinColumn(name = "menu_id"),
            inverseJoinColumns = @JoinColumn(name = "allergy_id")
    )
    private Set<Allergy> allergies = new HashSet<>();

    @Column(nullable = false)
    private Boolean isDeleted = false;

    @Column(name = "likes_count", nullable = false)
    private int likesCount = 0;

    @Column(name = "view_count", nullable = false)
    private int viewCount = 0;

    public Menu(String name, int price, Store store, Set<Allergy> allergies) {
        this.name = name;
        this.price = price;
        this.store = store;
        this.allergies = allergies;
    }

    // 메뉴 업데이트 메서드
    public void updateMenu(String name, int price, Set<Allergy> allergies) {
        this.name = name;
        this.price = price;
        this.allergies = allergies;
    }

    public void deleteMenu() {
        this.isDeleted = true;
    }

    // 좋아요 증가 메서드
    public void incrementLikes() {
        this.likesCount++;
    }

    // 조회수 증가 메서드
    public void incrementViews() {
        this.viewCount++;
    }
}
