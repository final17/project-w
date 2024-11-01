package com.projectw.domain.like.repository;

import com.projectw.domain.like.entity.Like;
import com.projectw.domain.menu.entity.Menu;
import com.projectw.domain.review.entity.Review;
import com.projectw.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, Long> {
    // 리뷰 좋아요 관련 메서드
    Optional<Like> findByReviewAndUser(Review review, User user);
    boolean existsByReviewAndUser(Review review, User user);
    long countByReview(Review review);
    // 메뉴 좋아요 관련 메서드
    Optional<Like> findByMenuAndUser(Menu menu, User user);
    boolean existsByMenuAndUser(Menu menu, User user);
    long countByMenu(Menu menu);
}
