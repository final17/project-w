package com.projectw.domain.review.controller;

import com.projectw.common.enums.UserRole;
import com.projectw.domain.review.dto.ReviewRequest;
import com.projectw.domain.review.dto.ReviewResponse;
import com.projectw.domain.review.service.ReviewServiceImpl;
import com.projectw.security.AuthUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;
class ReviewControllerTest {

    private ReviewController reviewController;
    private ReviewServiceImpl reviewService;

    @BeforeEach
    void setup() {
        reviewService = mock(ReviewServiceImpl.class);
        reviewController = new ReviewController(reviewService);
    }

    @Test
    @DisplayName("리뷰 생성 성공 테스트")
    void createReview_Success() {
        // given
        Long storeId = 1L;
        Long menuId = 1L;
        AuthUser authUser = new AuthUser(1L, "test@example.com", UserRole.ROLE_USER);
        ReviewRequest.Create request = new ReviewRequest.Create("Test Title", "Test Content", 5);
        List<MultipartFile> images = new ArrayList<>();

        ReviewResponse.Info expectedResponse = new ReviewResponse.Info(
                1L, "Test Title", "Test Content", 5,
                "2024-01-01T00:00:00", "2024-01-01T00:00:00",
                "TestUser", "test@example.com",
                List.of("image1.jpg"), 0L
        );

        when(reviewService.createReview(storeId, menuId, request, "test@example.com", images))
                .thenReturn(expectedResponse);

        // when
        ReviewResponse.Info actualResponse = reviewController.createReview(storeId, menuId, request, images, authUser).getBody();

        // then
        assertThat(actualResponse).isEqualTo(expectedResponse);
        verify(reviewService).createReview(storeId, menuId, request, "test@example.com", images);
    }

    @Test
    @DisplayName("메뉴별 리뷰 목록 조회 테스트")
    void getMenuReviews_Success() {
        // given
        Long menuId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        List<ReviewResponse.Info> reviews = List.of(
                new ReviewResponse.Info(1L, "Title1", "Content1", 5,
                        "2024-01-01T00:00:00", "2024-01-01T00:00:00",
                        "User1", "user1@test.com", List.of("image1.jpg"), 0L)
        );
        Page<ReviewResponse.Info> expectedResponse = new PageImpl<>(reviews, pageable, reviews.size());

        when(reviewService.getMenuReviews(menuId, pageable)).thenReturn(expectedResponse);

        // when
        Page<ReviewResponse.Info> actualResponse = reviewController.getMenuReviews(menuId, 0, 10).getBody();

        // then
        assertThat(actualResponse).isEqualTo(expectedResponse);
        verify(reviewService).getMenuReviews(menuId, pageable);
    }

    @Test
    @DisplayName("스토어별 리뷰 목록 조회 테스트")
    void getStoreReviews_Success() {
        // given
        Long storeId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        List<ReviewResponse.Info> reviews = List.of(
                new ReviewResponse.Info(1L, "Title1", "Content1", 5,
                        "2024-01-01T00:00:00", "2024-01-01T00:00:00",
                        "User1", "user1@test.com", List.of("image1.jpg"), 0L)
        );
        Page<ReviewResponse.Info> expectedResponse = new PageImpl<>(reviews, pageable, reviews.size());

        when(reviewService.getStoreReviews(storeId, pageable)).thenReturn(expectedResponse);

        // when
        Page<ReviewResponse.Info> actualResponse = reviewController.getStoreReviews(storeId, 0, 10).getBody();

        // then
        assertThat(actualResponse).isEqualTo(expectedResponse);
        verify(reviewService).getStoreReviews(storeId, pageable);
    }

    @Test
    @DisplayName("리뷰 수정 테스트")
    void updateReview_Success() {
        // given
        Long reviewId = 1L;
        AuthUser authUser = new AuthUser(1L, "test@example.com", UserRole.ROLE_USER);
        List<MultipartFile> newImages = new ArrayList<>();
        ReviewRequest.Update request = new ReviewRequest.Update("Updated Content", 4, newImages, List.of(1L));

        ReviewResponse.Info expectedResponse = new ReviewResponse.Info(
                1L, "Test Title", "Updated Content", 4,
                "2024-01-01T00:00:00", "2024-01-01T00:00:00",
                "TestUser", "test@example.com",
                List.of("image1.jpg"), 0L
        );

        when(reviewService.updateReview(reviewId, request, "test@example.com", newImages))
                .thenReturn(expectedResponse);

        // when
        ReviewResponse.Info actualResponse = reviewController.updateReview(reviewId, request, newImages, authUser).getBody();

        // then
        assertThat(actualResponse).isEqualTo(expectedResponse);
        verify(reviewService).updateReview(reviewId, request, "test@example.com", newImages);
    }


    @Test
    @DisplayName("리뷰 삭제 테스트")
    void deleteReview_Success() {
        // given
        Long reviewId = 1L;
        AuthUser authUser = new AuthUser(1L, "test@example.com", UserRole.ROLE_USER);
        ReviewResponse.Info expectedResponse = new ReviewResponse.Info(
                1L, "Test Title", "Test Content", 5,
                "2024-01-01T00:00:00", "2024-01-01T00:00:00",
                "TestUser", "test@example.com",
                List.of("image1.jpg"), 0L
        );

        when(reviewService.deleteReview(reviewId, "test@example.com")).thenReturn(expectedResponse);

        // when
        ReviewResponse.Info actualResponse = reviewController.deleteReview(reviewId, authUser).getBody();

        // then
        assertThat(actualResponse).isEqualTo(expectedResponse);
        verify(reviewService).deleteReview(reviewId, "test@example.com");
    }

    @Test
    @DisplayName("내 리뷰 목록 조회 테스트")
    void getUserReviews_Success() {
        // given
        AuthUser authUser = new AuthUser(1L, "test@example.com", UserRole.ROLE_USER);
        Pageable pageable = PageRequest.of(0, 10);
        List<ReviewResponse.Info> reviews = List.of(
                new ReviewResponse.Info(1L, "Title1", "Content1", 5,
                        "2024-01-01T00:00:00", "2024-01-01T00:00:00",
                        "User1", "test@example.com", List.of("image1.jpg"), 0L)
        );
        Page<ReviewResponse.Info> expectedResponse = new PageImpl<>(reviews, pageable, reviews.size());

        when(reviewService.getUserReviews("test@example.com", pageable)).thenReturn(expectedResponse);

        // when
        Page<ReviewResponse.Info> actualResponse = reviewController.getUserReviews(0, 10, authUser).getBody();

        // then
        assertThat(actualResponse).isEqualTo(expectedResponse);
        verify(reviewService).getUserReviews("test@example.com", pageable);
    }
}
