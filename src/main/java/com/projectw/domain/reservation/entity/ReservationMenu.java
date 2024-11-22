package com.projectw.domain.reservation.entity;

import com.projectw.common.entity.Timestamped;
import com.projectw.domain.menu.entity.Menu;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "reservation_menu")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReservationMenu extends Timestamped {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 메뉴아이디
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_id", nullable = false)
    private Menu menu;

    // 메뉴명
    @Column(nullable = false)
    private String menuName;

    // 메뉴가격
    @Column(nullable = false)
    private Long menuPrice;

    // 메뉴개수
    @Column(nullable = false)
    private Long menuCnt;

    // 예약아이디
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    public ReservationMenu(Menu menu , String menuName , Long menuPrice , Long menuCnt , Reservation reservation) {
        this.menu = menu;
        this.menuName = menuName;
        this.menuPrice = menuPrice;
        this.menuCnt = menuCnt;
        this.reservation = reservation;
    }

}
