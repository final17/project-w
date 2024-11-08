package com.projectw.domain.review.service;

import com.projectw.common.config.S3Service;
import com.projectw.domain.like.repository.LikeRepository;
import com.projectw.domain.menu.entity.Menu;
import com.projectw.domain.menu.repository.MenuRepository;
import com.projectw.domain.reservation.entity.Reservation;
import com.projectw.domain.reservation.enums.ReservationStatus;
import com.projectw.domain.reservation.repository.ReservationRepository;
import com.projectw.domain.review.dto.request.ReviewRequestDto;
import com.projectw.domain.review.dto.request.ReviewUpdateDto;
import com.projectw.domain.review.dto.response.ReviewResponseDto;
import com.projectw.domain.review.entity.Review;
import com.projectw.domain.review.entity.ReviewImage;
import com.projectw.domain.review.repository.ReviewRepository;
import com.projectw.domain.user.entity.User;
import com.projectw.domain.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final MenuRepository menuRepository;
    private final UserRepository userRepository;
    private final ReservationRepository reservationRepository;
    private final LikeRepository likeRepository;
    private final S3Service s3Service;

    @Transactional
    public ReviewResponseDto createReview(Long menuId, ReviewRequestDto reviewRequestDto, String email, List<MultipartFile> images) {
        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new IllegalArgumentException("메뉴를 찾을 수 없습니다."));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("권한이 없습니다."));

        Reservation reservation = reservationRepository.findByUserAndStore(user, menu.getStore())
                .orElseThrow(() -> new IllegalArgumentException("해당 매장의 예약 내역이 없습니다."));

        if (!reservation.getStatus().equals(ReservationStatus.COMPLETE)) {
            throw new IllegalArgumentException("방문 완료된 예약에 대해서만 리뷰를 작성할 수 있습니다.");
        }

        if (reviewRepository.existsByUserAndMenu(user, menu)) {
            throw new IllegalArgumentException("이미 리뷰를 작성하셨습니다.");
        }

        Review review = Review.builder()
                .title(reviewRequestDto.getTitle())
                .content(reviewRequestDto.getContent())
                .rating(reviewRequestDto.getRating())
                .reservation(reservation)
                .build();

        processImages(images, review);
        Review savedReview = reviewRepository.save(review);
        return ReviewResponseDto.fromWithMenu(savedReview, 0L, false, menu);
    }

    public Page<ReviewResponseDto> getMenuReviews(Long menuId, Pageable pageable) {
        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new IllegalArgumentException("메뉴를 찾을 수 없습니다."));

        Page<Object[]> reviewsWithCount = reviewRepository.findAllByMenuWithUserAndLikeCount(menu, pageable);

        return reviewsWithCount.map(objects -> {
            Review review = (Review) objects[0];
            Long likeCount = (Long) objects[1];

            return ReviewResponseDto.builder()
                    .id(review.getId())
                    .title(review.getTitle())
                    .content(review.getContent())
                    .rating(review.getRating())
                    .nickname(review.getUser().getNickname())
                    .createdAt(review.getCreatedAt())
                    .likeCount(likeCount)
                    .imageUrls(review.getImages().stream()
                            .map(ReviewImage::getImageUrl)
                            .toList())
                    .build();
        });
    }

    @Transactional
    public ReviewResponseDto updateReview(Long reviewId, ReviewUpdateDto updateDto, String email) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다."));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (!review.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("본인의 리뷰만 수정할 수 있습니다.");
        }

        // 이미지 삭제 처리
        if (updateDto.getDeleteImageIds() != null && !updateDto.getDeleteImageIds().isEmpty()) {
            review.getImages().stream()
                    .filter(image -> updateDto.getDeleteImageIds().contains(image.getId()))
                    .forEach(image -> {
                        try {
                            deleteImage(image);
                        } catch (Exception e) {
                            log.error("이미지 삭제 실패: {}", image.getImageUrl(), e);
                        }
                    });
            review.getImages().removeIf(image ->
                    updateDto.getDeleteImageIds().contains(image.getId()));
        }

        processImages(updateDto.getNewImages(), review);
        review.update(updateDto.getContent(), updateDto.getRating());

        Long likeCount = likeRepository.countByReview(review);
        boolean liked = likeRepository.existsByReviewAndUser(review, user);

        return ReviewResponseDto.from(review, likeCount, liked);
    }

    @Transactional
    public ReviewResponseDto deleteReview(Long reviewId, String email) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다."));
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("삭제 권한이 없습니다."));

        // 리뷰 이미지 삭제
        review.getImages().forEach(this::deleteImage);

        reviewRepository.delete(review);
        Long likeCount = likeRepository.countByReview(review);
        boolean liked = likeRepository.existsByReviewAndUser(review, user);

        return ReviewResponseDto.from(review, likeCount, liked);
    }

    @Transactional
    public void processImages(List<MultipartFile> images, Review review) {
        if (images == null || images.isEmpty()) return;

        for (MultipartFile image : images) {
            try {
                String imageUrl = s3Service.uploadFile(image);
                ReviewImage reviewImage = ReviewImage.builder()
                        .imageUrl(imageUrl)
                        .review(review)
                        .build();
                review.addImage(reviewImage);
            } catch (IOException e) {
                log.error("이미지 업로드 실패", e);
                throw new RuntimeException("이미지 업로드 중 오류가 발생했습니다: " + e.getMessage());
            }
        }
    }

    private void deleteImage(ReviewImage image) {
        try {
            String fileName = image.getImageUrl().substring(image.getImageUrl().lastIndexOf("/") + 1);
            s3Service.deleteFile(fileName);
        } catch (Exception e) {
            log.error("S3 이미지 삭제 실패: {}", image.getImageUrl(), e);
            throw new RuntimeException("이미지 삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}