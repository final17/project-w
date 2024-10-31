package com.projectw.domain.like.service;

import com.projectw.domain.like.entity.Like;
import com.projectw.domain.like.repository.LikeRepository;
import com.projectw.domain.menu.entity.Menu;
import com.projectw.domain.menu.repository.MenuLikeRepository;
import com.projectw.domain.menu.repository.MenuRepository;
import com.projectw.domain.review.entity.Review;
import com.projectw.domain.review.repository.ReviewRepository;
import com.projectw.domain.user.entity.User;
import com.projectw.domain.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class LikeService {
    private final ReviewRepository reviewRepository;
    private final MenuRepository menuRepository;
    private final LikeRepository reviewLikeRepository;
    private final MenuLikeRepository menuLikeRepository;
    private final UserRepository userRepository;
    private final RedissonClient redissonClient;

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

    @Transactional
    public boolean toggleLikeForMenu(Long menuId, Long userId) {
        String lockKey = "lock:menu:like:" + menuId; // 메뉴 ID 기반으로 고유 락 키 생성
        RLock lock = redissonClient.getLock(lockKey);

        try {
            // 락을 시도 (10초 이내에 락을 획득할 수 있어야 함, 획득 후 2초 동안 유지)
            if (lock.tryLock(10, 2, TimeUnit.SECONDS)) {
                // 메뉴 조회
                Menu menu = menuRepository.findById(menuId)
                        .orElseThrow(() -> new IllegalArgumentException("해당 메뉴를 찾을 수 없습니다."));
                // 사용자 조회
                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

                // 메뉴에 대한 기존 좋아요 조회
                Optional<Like> existingLike = menuLikeRepository.findByMenuAndUser(menu, user);

                if (existingLike.isPresent()) {
                    // 좋아요가 이미 존재하면 취소
                    menuLikeRepository.delete(existingLike.get());
                    menu.decrementLikes();
                    menuRepository.save(menu);
                    return false; // 좋아요 취소
                } else {
                    // 좋아요가 존재하지 않으면 추가
                    menuLikeRepository.save(new Like(menu, user));
                    menu.incrementLikes();
                    menuRepository.save(menu);
                    return true; // 좋아요 추가
                }
            } else {
                throw new RuntimeException("잠금 획득에 실패했습니다. 잠시 후 다시 시도해주세요.");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("잠금 획득 중 인터럽트가 발생했습니다.", e);
        } finally {
            lock.unlock(); // 락 해제
        }
    }

    public long getLikeCount(Long reviewId) {
        Review review = findReview(reviewId);
        return reviewLikeRepository.countByReview(review);
    }

    public long getLikeCountForMenu(Long menuId) {
        Menu menu = findMenu(menuId);
        return menuLikeRepository.countByMenu(menu);
    }

    public boolean hasLiked(Long reviewId, String email) {
        Review review = findReview(reviewId);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        return reviewLikeRepository.existsByReviewAndUser(review, user);
    }

    public boolean hasLikedMenu(Long menuId, Long userId) {
        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new IllegalArgumentException("해당 메뉴를 찾을 수 없습니다."));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        return menuLikeRepository.existsByMenuAndUser(menu, user);
    }

    private Review findReview(Long reviewId) {
        return reviewRepository.findById(reviewId).orElseThrow(()-> new IllegalArgumentException("리뷰를 찾을 수 없습니다."));
    }

    private Menu findMenu(Long menuId) {
        return menuRepository.findById(menuId)
                .orElseThrow(() -> new IllegalArgumentException("메뉴를 찾을 수 없습니다."));
    }
}
