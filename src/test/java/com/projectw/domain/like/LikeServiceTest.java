package com.projectw.domain.like;

import com.projectw.domain.like.dto.LikeResponseDto;
import com.projectw.domain.like.entity.Like;
import com.projectw.domain.like.repository.LikeRepository;
import com.projectw.domain.like.service.LikeService;
import com.projectw.domain.menu.entity.Menu;
import com.projectw.domain.menu.repository.MenuRepository;
import com.projectw.domain.review.entity.Review;
import com.projectw.domain.review.repository.ReviewRepository;
import com.projectw.domain.user.entity.User;
import com.projectw.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LikeServiceTest {

    @InjectMocks
    private LikeService likeService;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private MenuRepository menuRepository;

    @Mock
    private LikeRepository likeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RedissonClient redissonClient;

    @Mock
    private RLock lock;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void toggleReviewLike_WhenNotLiked_ShouldSaveLike() throws InterruptedException {
        Long reviewId = 1L;
        String email = "test@example.com";

        Review review = mock(Review.class);
        User user = mock(User.class);
        Like like = mock(Like.class);

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(redissonClient.getLock("lock:like:review:" + reviewId)).thenReturn(lock);
        when(lock.tryLock(5, 2, TimeUnit.SECONDS)).thenReturn(true);
        when(lock.isHeldByCurrentThread()).thenReturn(true); // Mock 설정 추가
        when(likeRepository.findByReviewAndUser(review, user)).thenReturn(Optional.empty());
        when(likeRepository.save(any(Like.class))).thenReturn(like);

        boolean result = likeService.toggleReviewLike(reviewId, email);

        assertTrue(result);
        verify(likeRepository, times(1)).save(any(Like.class));
        verify(lock, times(1)).unlock(); // unlock 호출 검증
    }

    @Test
    void toggleReviewLike_WhenAlreadyLiked_ShouldRemoveLike() throws InterruptedException {
        Long reviewId = 1L;
        String email = "test@example.com";

        Review review = mock(Review.class);
        User user = mock(User.class);
        Like like = mock(Like.class);

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(redissonClient.getLock("lock:like:review:" + reviewId)).thenReturn(lock);
        when(lock.tryLock(5, 2, TimeUnit.SECONDS)).thenReturn(true);
        when(lock.isHeldByCurrentThread()).thenReturn(true); // Mock 설정 추가
        when(likeRepository.findByReviewAndUser(review, user)).thenReturn(Optional.of(like));

        boolean result = likeService.toggleReviewLike(reviewId, email);

        assertFalse(result);
        verify(likeRepository, times(1)).delete(like);
        verify(lock, times(1)).unlock(); // unlock 호출 검증
    }

    @Test
    void getMenuLikeStatus_ShouldReturnCorrectStatus() {
        Long storeId = 1L;
        Long menuId = 1L;
        Long userId = 1L;

        Menu menu = mock(Menu.class);
        User user = mock(User.class);

        when(menuRepository.findByIdAndStoreId(menuId, storeId)).thenReturn(Optional.of(menu));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(likeRepository.existsByMenuAndUser(menu, user)).thenReturn(true);
        when(likeRepository.countByMenu(menu)).thenReturn(10L);

        LikeResponseDto.Basic result = likeService.getMenuLikeStatus(storeId, menuId, userId);

        assertTrue(result.liked());
        assertEquals(10L, result.likeCount());
    }

    @Test
    void getLikeCount_ShouldReturnCorrectCount() {
        Long reviewId = 1L;
        Review review = mock(Review.class);

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
        when(likeRepository.countByReview(review)).thenReturn(5L);

        long result = likeService.getLikeCount(reviewId);

        assertEquals(5L, result);
    }

    @Test
    void hasLikedReview_ShouldReturnCorrectStatus() {
        Long reviewId = 1L;
        String email = "test@example.com";

        Review review = mock(Review.class);
        User user = mock(User.class);

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(likeRepository.existsByReviewAndUser(review, user)).thenReturn(true);

        boolean result = likeService.hasLikedReview(reviewId, email);

        assertTrue(result);
    }
}