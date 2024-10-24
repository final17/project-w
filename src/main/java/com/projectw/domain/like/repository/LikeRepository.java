package com.projectw.domain.like.repository;

import com.projectw.domain.like.entity.Like;
import com.projectw.domain.review.entity.Review;
import com.projectw.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, Long> {
    Optional<Like> findByReviewAndUser(Review review, User user);
    boolean existsByReviewAndUser(Review review, User user);
    Long countByReview(Review review);

}
