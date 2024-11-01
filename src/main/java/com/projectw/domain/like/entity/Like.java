package com.projectw.domain.like.entity;

import com.projectw.common.entity.Timestamped;
import com.projectw.domain.menu.entity.Menu;
import com.projectw.domain.review.entity.Review;
import com.projectw.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "likes")
public class Like extends Timestamped {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id")
    private Review review;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "menu_id")
    private Menu menu;

    @Builder
    private Like(Review review, User user) {
        this.review = review;
        this.user = user;
    }

    public Like(Menu menu, User user) {
        this.menu = menu;
        this.user = user;
    }

    public static Like forReview(Review review, User user) {
        return new Like(review, user);
    }

    public static Like forMenu(Menu menu, User user) {
        return new Like(menu, user);
    }
}
