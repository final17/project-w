package com.projectw.domain.like.service;

import com.projectw.domain.like.dto.Response.LikeResponseDto;
import com.projectw.domain.like.entity.Like;
import com.projectw.domain.like.repository.LikeRepository;
import com.projectw.domain.menu.entity.Menu;
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
import java.util.function.BiFunction;

@Service
@RequiredArgsConstructor
public class LikeService {
    private final ReviewRepository reviewRepository;
    private final MenuRepository menuRepository;
    private final LikeRepository likeRepository;
    private final UserRepository userRepository;
    private final RedissonClient redissonClient;

    // 리뷰 좋아요 토글
    @Transactional
    public boolean toggleReviewLike(Long reviewId, String email) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다."));
        User user = findUserByEmail(email);
        return toggleLike(review, user, likeRepository::findByReviewAndUser, lockKeyForReview(reviewId));
    }

    // 메뉴 좋아요 토글
    @Transactional
    public boolean toggleMenuLike(Long storeId, Long menuId, Long userId) {
        Menu menu = findMenu(storeId, menuId);
        User user = findUserById(userId);
        return toggleLike(menu, user, likeRepository::findByMenuAndUser, lockKeyForMenu(storeId, menuId));
    }

    // 공통 좋아요 토글 메서드
    private <T> boolean toggleLike(T entity, User user, BiFunction<T, User, Optional<Like>> findLike, String lockKey) {
        RLock lock = redissonClient.getLock(lockKey);

        try {
            if (lock.tryLock(10, 2, TimeUnit.SECONDS)) {
                Optional<Like> existingLike = findLike.apply(entity, user);

                if (existingLike.isPresent()) {
                    likeRepository.delete(existingLike.get());
                    return false; // 좋아요 취소
                } else {
                    Like like = entity instanceof Review ? Like.forReview((Review) entity, user) : Like.forMenu((Menu) entity, user);
                    likeRepository.save(like);
                    return true; // 좋아요 추가
                }
            } else {
                throw new RuntimeException("Rock 획득에 실패했습니다. 잠시 후 다시 시도해주세요.");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Rock 획득 중 인터럽트가 발생했습니다.", e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
                }
        }
    }

    // 리뷰에 대한 좋아요 수 조회
    public long getLikeCount(Long reviewId) {
        Review review = findReview(reviewId);
        return likeRepository.countByReview(review);
    }

    // 좋아요 상태 확인
    public boolean hasLikedReview(Long reviewId, String email) {
        Review review = findReview(reviewId);
        User user = findUserByEmail(email);
        return likeRepository.existsByReviewAndUser(review, user);
    }

    public LikeResponseDto getMenuLikeStatus(Long storeId, Long menuId, Long userId) {
        Menu menu = findMenu(storeId, menuId);
        User user = findUserById(userId);
        boolean liked = likeRepository.existsByMenuAndUser(menu, user);
        long likeCount = likeRepository.countByMenu(menu);
        return new LikeResponseDto(liked, likeCount);  // 좋아요 상태와 갯수 반환
    }

    // 보조 메서드
    private Review findReview(Long reviewId) {
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다."));
    }

    private Menu findMenu(Long storeId, Long menuId) {
        return menuRepository.findByIdAndStoreId(menuId, storeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 가게에 메뉴가 없습니다."));
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }

    // 락 키 생성 메서드
    private String lockKeyForReview(Long reviewId) {
        return "lock:like:review:" + reviewId;
    }

    private String lockKeyForMenu(Long storeId, Long menuId) {
        return "lock:like:menu:" + storeId + ":" + menuId;
    }
}