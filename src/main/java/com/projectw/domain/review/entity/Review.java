package com.projectw.domain.review.entity;

import com.projectw.common.entity.Timestamped;
import com.projectw.domain.like.entity.Like;
import com.projectw.domain.menu.entity.Menu;
import com.projectw.domain.reservation.entity.Reservation;
import com.projectw.domain.store.entity.Store;
import com.projectw.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@Table(name = "reviews")
@Getter

public class Review extends Timestamped {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private int rating;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private Store store;

    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReviewImage> images = new ArrayList<>();

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id")
    private Reservation reservation;

    @ManyToOne(fetch = FetchType.LAZY)  // Menu와의 관계 추가
    @JoinColumn(name = "menu_id")
    private Menu menu;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Like> likes = new ArrayList<>();

    @Builder
    public Review(String title, String content, int rating, Reservation reservation, Menu menu) {
        this.title = title;
        this.content = content;
        this.rating = rating;
        this.reservation = reservation;
        this.menu = menu;
        this.user = reservation.getUser();
        this.store = reservation.getStore();
    }

    public void addImage(ReviewImage image) {
        this.images.add(image);
    }

    public void update(String content, int rating) {
        this.content = content;
        this.rating = rating;
    }
}
