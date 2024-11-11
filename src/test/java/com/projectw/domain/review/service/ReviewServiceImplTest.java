package com.projectw.domain.review.service;
import com.projectw.domain.like.repository.LikeRepository;
import com.projectw.domain.menu.entity.Menu;
import com.projectw.domain.menu.repository.MenuRepository;
import com.projectw.domain.reservation.entity.Reservation;
import com.projectw.domain.reservation.enums.ReservationStatus;
import com.projectw.domain.reservation.repository.ReservationRepository;
import com.projectw.domain.review.dto.ReviewRequest;
import com.projectw.domain.review.dto.ReviewResponse;
import com.projectw.domain.review.entity.Review;
import com.projectw.domain.review.entity.ReviewImage;
import com.projectw.domain.review.repository.ReviewRepository;
import com.projectw.domain.store.entity.Store;
import com.projectw.domain.store.repository.StoreRepository;
import com.projectw.domain.user.entity.User;
import com.projectw.domain.user.repository.UserRepository;
import com.projectw.common.config.S3Service;
import jakarta.persistence.EntityManager;
import org.aspectj.lang.annotation.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class ReviewServiceImplTest {

    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private MenuRepository menuRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private LikeRepository likeRepository;
    @Mock
    private StoreRepository storeRepository;
    @Mock
    private S3Service s3Service;

    private ReviewServiceImpl reviewService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        reviewService = new ReviewServiceImpl(reviewRepository, menuRepository, userRepository, reservationRepository, likeRepository, storeRepository, s3Service);
    }

    @Test
    void 리뷰를생성한다() {
        User user = mock(User.class);
        when(user.getEmail()).thenReturn("test@example.com");

        Store store = mock(Store.class);
        when(store.getId()).thenReturn(1L); // 예시로 store ID 설정

        Menu menu = mock(Menu.class);
        when(menu.getStore()).thenReturn(store); // menu에서 store 반환
        when(menuRepository.findById(anyLong())).thenReturn(Optional.of(menu)); // 메뉴 조회 Mock

        ReviewRequest.Create reviewRequestDto = mock(ReviewRequest.Create.class);
        when(reviewRequestDto.title()).thenReturn("Great Menu");
        when(reviewRequestDto.content()).thenReturn("I love this menu!");
        when(reviewRequestDto.rating()).thenReturn(5);

        MultipartFile image = mock(MultipartFile.class);
        when(image.getOriginalFilename()).thenReturn("test_image.jpg");

        Reservation reservation = mock(Reservation.class);
        when(reservation.getStatus()).thenReturn(ReservationStatus.valueOf(ReservationStatus.Status.COMPLETE)); // 상태 설정

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(reservationRepository.findByUserAndStore(any(), any())).thenReturn(Optional.of(reservation));
        when(reviewRepository.existsByUserAndMenu(any(), any())).thenReturn(false);
        when(s3Service.uploadFile(any())).thenReturn("http://s3.amazon.com/test_image.jpg");

        when(storeRepository.findById(anyLong())).thenReturn(Optional.of(store));

        Review review = mock(Review.class);
        when(review.getId()).thenReturn(1L);
        when(review.getTitle()).thenReturn("Great Menu");
        when(review.getContent()).thenReturn("I love this menu!");
        when(review.getRating()).thenReturn(5);
        when(review.getCreatedAt()).thenReturn(LocalDateTime.now()); // Mocking createdAt
        when(review.getUpdatedAt()).thenReturn(LocalDateTime.now()); // Mocking updatedAt

        when(reviewRepository.save(any(Review.class))).thenReturn(review);

        ReviewResponse.Info reviewInfo = reviewService.createReview(1L, 1L, reviewRequestDto, "test@example.com", List.of(image));

        assertNotNull(reviewInfo);
        assertEquals("Great Menu", reviewInfo.title());
        verify(reviewRepository, times(1)).save(any(Review.class));
    }

    @Test
    void getMenuReviewsTest() {
        // Mocking Menu
        Menu menu = mock(Menu.class);
        when(menuRepository.findById(anyLong())).thenReturn(Optional.of(menu));

        // Mocking Review objects
        Review review1 = mock(Review.class);
        Review review2 = mock(Review.class);

        User user1 = mock(User.class);
        User user2 = mock(User.class);

        // Mocking ReviewImages and their URLs
        ReviewImage image1 = mock(ReviewImage.class);
        ReviewImage image2 = mock(ReviewImage.class);

        when(image1.getImageUrl()).thenReturn("http://s3.amazon.com/image1.jpg");
        when(image2.getImageUrl()).thenReturn("http://s3.amazon.com/image2.jpg");

        List<ReviewImage> images1 = List.of(image1);
        List<ReviewImage> images2 = List.of(image2);

        when(review1.getUser()).thenReturn(user1);
        when(review2.getUser()).thenReturn(user2);
        when(review1.getImages()).thenReturn(images1);
        when(review2.getImages()).thenReturn(images2);

        when(review1.getId()).thenReturn(1L);
        when(review2.getId()).thenReturn(2L);
        when(review1.getTitle()).thenReturn("Review 1");
        when(review2.getTitle()).thenReturn("Review 2");

        // Mocking createdAt and updatedAt (set non-null values)
        when(review1.getCreatedAt()).thenReturn(LocalDateTime.now());
        when(review2.getCreatedAt()).thenReturn(LocalDateTime.now());
        when(review1.getUpdatedAt()).thenReturn(LocalDateTime.now());  // Mocking updatedAt
        when(review2.getUpdatedAt()).thenReturn(LocalDateTime.now());  // Mocking updatedAt

        Long likeCount1 = 10L;
        Long likeCount2 = 20L;

        // Mocking reviewRepository
        List<Object[]> reviews = List.of(
                new Object[]{review1, likeCount1},
                new Object[]{review2, likeCount2}
        );

        Page<Object[]> page = new PageImpl<>(reviews);
        when(reviewRepository.findAllByMenuWithUserAndLikeCount(menu, Pageable.unpaged())).thenReturn(page);

        // Calling the method
        Page<ReviewResponse.Info> result = reviewService.getMenuReviews(1L, Pageable.unpaged());

        // Assertions
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());

        // Checking the first review
        ReviewResponse.Info reviewInfo1 = result.getContent().get(0);
        assertEquals("Review 1", reviewInfo1.title());
        assertEquals(10L, reviewInfo1.likeCount());
        assertTrue(reviewInfo1.imageUrls().contains("http://s3.amazon.com/image1.jpg"));

        // Checking the second review
        ReviewResponse.Info reviewInfo2 = result.getContent().get(1);
        assertEquals("Review 2", reviewInfo2.title());
        assertEquals(20L, reviewInfo2.likeCount());
        assertTrue(reviewInfo2.imageUrls().contains("http://s3.amazon.com/image2.jpg"));
    }


    @Test
    void updateReviewTest() {
        // Mocking User
        User user = mock(User.class);
        given(user.getId()).willReturn(1L);
        given(userRepository.findByEmail(anyString())).willReturn(Optional.of(user));

        // Mocking Review
        Review review = mock(Review.class);
        given(reviewRepository.findById(anyLong())).willReturn(Optional.of(review));
        given(review.getUser()).willReturn(user);

        // Mocking Review's createdAt to avoid NullPointerException
        given(review.getCreatedAt()).willReturn(LocalDateTime.now());
        given(review.getUpdatedAt()).willReturn(LocalDateTime.now());


        // Mocking Review Images
        ReviewImage image1 = mock(ReviewImage.class);
        given(image1.getId()).willReturn(1L);
        given(image1.getImageUrl()).willReturn("http://s3.amazon.com/image1.jpg");
        List<ReviewImage> images = new ArrayList<>();
        images.add(image1);
        given(review.getImages()).willReturn(images);

        // Actual implementation of ReviewRequest.Update object
        ReviewRequest.Update updateDto = mock(ReviewRequest.Update.class);
        given(updateDto.content()).willReturn("Updated content");
        given(updateDto.rating()).willReturn(4);
        given(updateDto.deleteImageIds()).willReturn(List.of(1L));
        given(updateDto.newImages()).willReturn(List.of(mock(MultipartFile.class)));
        // Verifying that the given condition works
        System.out.println("Content before update: " + updateDto.content());  // Should print "Updated content"
        System.out.println("Rating before update: " + updateDto.rating());  // Should print 4

        // Mocking Review update and like count
        given(likeRepository.countByReview(any(Review.class))).willReturn(10L);
        given(likeRepository.existsByReviewAndUser(any(Review.class), any(User.class))).willReturn(false);

        // Calling the method
        ReviewResponse.Info updatedReviewInfo = reviewService.updateReview(1L, updateDto, "test@example.com", List.of(mock(MultipartFile.class)));

// Check if updatedReviewInfo is null
        assertNotNull(updatedReviewInfo, "Review update info should not be null");

// Check the actual content
//        assertEquals("Updated content", updatedReviewInfo.content());
    }

    @Test
    void 리뷰수정_중_리뷰를_찾을수_없을때() {
        // Mocking Review not found scenario
        when(reviewRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Mocking User
        User user = mock(User.class);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));

        // Calling the method and expecting an exception
        assertThrows(IllegalArgumentException.class, () -> reviewService.updateReview(999L, mock(ReviewRequest.Update.class), "test@example.com", List.of()));
    }

    @Test
    void 리뷰_업데이트_중_유저를_찾을_수_없을때() {
        // Mocking User with different IDs
        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));

        // Mocking Review with a different user
        Review review = mock(Review.class);
        when(reviewRepository.findById(anyLong())).thenReturn(Optional.of(review));
        when(review.getUser()).thenReturn(mock(User.class));  // Different user

        // Calling the method and expecting an exception
        assertThrows(IllegalArgumentException.class, () -> reviewService.updateReview(1L, mock(ReviewRequest.Update.class), "test@example.com", List.of()));
    }

//
//    @Test
//    void testGetMenuReviews() {
//        // Review 객체 생성 (record 타입이라 생성자로 초기화)
//        Review review = new Review("title", "contents", 5, ); // 생성자 방식 사용
//
//        // Like count 예시
//        Long likeCount = 10L;
//
//        // Page<Object[]> 반환 설정
//        Object[] reviewWithLikeCount = { review, likeCount };
//        PageImpl<Object[]> page = new PageImpl<>(List.of(reviewWithLikeCount), PageRequest.of(0, 10), 1);
//
//        // Mocking reviewRepository.findAllByMenuWithUserAndLikeCount 호출
//        when(reviewRepository.findAllByMenuWithUserAndLikeCount(any(Menu.class), any(Pageable.class)))
//                .thenReturn(page);
//
//        // 서비스 메서드 호출
//        Page<ReviewResponse.Info> result = reviewService.getMenuReviews(menu.getId(), PageRequest.of(0, 10));
//
//        // 검증
//        assertThat(result.getTotalElements()).isEqualTo(1);
//        ReviewResponse.Info reviewInfo = result.getContent().get(0);
//        assertThat(reviewInfo.id()).isEqualTo(1L);
//        assertThat(reviewInfo.title()).isEqualTo("Great Review");
//        assertThat(reviewInfo.likeCount()).isEqualTo(likeCount);
//    }

}