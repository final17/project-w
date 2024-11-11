package com.projectw.domain.review.dto;

import com.projectw.domain.menu.entity.Menu;
import com.projectw.domain.review.entity.Review;
import com.projectw.domain.user.entity.User;

public sealed interface ReviewResponse permits ReviewResponse.Info, ReviewResponse.Like {

    // ReviewResponse에 대한 Info 구현
    record Info(
            Long id,
            String title,
            String content,
            int rating,
            String createdAt,
            String updatedAt,
            String userNickname,
            String userEmail
    ) implements ReviewResponse {
        // Info를 생성하는 생성자
        public Info(Review review, User user) {
            this(
                    review.getId(),
                    review.getTitle(),
                    review.getContent(),
                    review.getRating(),
                    review.getCreatedAt().toString(),
                    review.getUpdatedAt().toString(),
                    user.getNickname(),
                    user.getEmail()
            );
        }
    }

    // ReviewResponse에 대한 Like 구현 (예시로 작성, 필요에 따라 수정)
    record Like(Long reviewId, String reviewContent, Boolean isLiked) implements ReviewResponse {
        // Like를 생성하는 생성자
        public Like(Review review, Boolean isLiked) {
            this(review.getId(), review.getContent(), isLiked);
        }
    }
}
