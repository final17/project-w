package com.projectw.domain.like.service;

import com.projectw.domain.like.entity.Like;
import com.projectw.domain.like.repository.LikeRepository;
import com.projectw.domain.review.entity.Review;
import com.projectw.domain.review.repository.ReviewRepository;
import com.projectw.domain.user.entity.User;
import com.projectw.domain.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@RequestMapping("/api/v1/")
public class LikeService {
    private final ReviewRepository reviewRepository;
    private final LikeRepository reviewLikeRepository;
    private final UserRepository userRepository;

    @Transactional
    public boolean toggleLike(Long reviewId, String email) {
        Review review = findReview(reviewId);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Optional<Like> existingLike = reviewLikeRepository.findByReviewAndUser(review, user);

        if (existingLike.isPresent()) {
            // 좋아요가 이미 있으면 삭제 (취소)
            reviewLikeRepository.delete(existingLike.get());
            return false;
        } else {
            // 좋아요가 없으면 생성
            Like reviewLike = Like.builder()
                    .review(review)
                    .user(user)
                    .build();
            reviewLikeRepository.save(reviewLike);
            return true;
        }
    }

    public long getLikeCount(Long reviewId) {
        Review review = findReview(reviewId);
        return reviewLikeRepository.countByReview(review);
    }

    public boolean hasLiked(Long reviewId, String email) {
        Review review = findReview(reviewId);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        return reviewLikeRepository.existsByReviewAndUser(review, user);
    }

    private Review findReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId).orElseThrow(()-> new IllegalArgumentException("리뷰를 찾을 수 없습니다."));
        return review;
    }

}
