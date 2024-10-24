package com.projectw.domain.review.service;

import com.projectw.common.exceptions.FileUploadException;
import com.projectw.common.utils.FileUtil;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final MenuRepository menuRepository;
    private final UserRepository userRepository;
    private final ReservationRepository reservationRepository;
    private final LikeRepository likeRepository;

    @Value("${DB_DIR}")
    private String fileDir;

    @Transactional
    public ReviewResponseDto createReview(Long menuId, ReviewRequestDto reviewRequestDto, String email, List<MultipartFile> images) {
        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new IllegalArgumentException("메뉴를 찾을 수 없습니다."));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("권한이 없습니다."));

        // 해당 사용자의 완료된 예약 확인
        Reservation reservation = reservationRepository.findByUserAndStore(user, menu.getStore())
                .orElseThrow(() -> new IllegalArgumentException("해당 매장의 예약 내역이 없습니다."));

        // 예약 상태가 완료인 경우에만 리뷰 작성 가능
        if (!reservation.getStatus().equals(ReservationStatus.COMPLETE)) {
            throw new IllegalArgumentException("방문 완료된 예약에 대해서만 리뷰를 작성할 수 있습니다.");
        }

        // 이미 리뷰를 작성했는지 확인
        if (reviewRepository.existsByUserAndMenu(user, menu)) {
            throw new IllegalArgumentException("이미 리뷰를 작성하셨습니다.");
        }

        Review review = Review.builder()
                .title(reviewRequestDto.getTitle())
                .content(reviewRequestDto.getContent())
                .rating(reviewRequestDto.getRating())
                .reservation(reservation)
                .build();

        // 이미지 업로드 처리
        if (images != null && !images.isEmpty()) {
            for (MultipartFile image : images) {
                try {
                    String savedFileName = uploadFile(image, email);
                    if (savedFileName != null) {
                        ReviewImage reviewImage = ReviewImage.builder()
                                .imageUrl(savedFileName)
                                .review(review)
                                .build();
                        review.addImage(reviewImage);
                    }
                } catch (IOException e) {
                    throw new IllegalArgumentException("이미지 업로드에 실패했습니다: " + e.getMessage());
                }
            }
        }

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

        // 리뷰 작성자 확인
        if (!review.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("본인의 리뷰만 수정할 수 있습니다.");
        }

        // 이미지 삭제 처리
        if (updateDto.getDeleteImageIds() != null && !updateDto.getDeleteImageIds().isEmpty()) {
            review.getImages().removeIf(image ->
                    updateDto.getDeleteImageIds().contains(image.getId()));
        }

        // 새로운 이미지 추가
        if (updateDto.getNewImages() != null && !updateDto.getNewImages().isEmpty()) {
            for (MultipartFile image : updateDto.getNewImages()) {
                try {
                    String savedFileName = uploadFile(image, email);
                    if (savedFileName != null) {
                        ReviewImage reviewImage = ReviewImage.builder()
                                .imageUrl(savedFileName)
                                .review(review)
                                .build();
                        review.addImage(reviewImage);
                    }
                } catch (IOException e) {
                    throw new FileUploadException("이미지 업로드에 실패했습니다: " + e.getMessage());
                }
            }
        }

        // 리뷰 내용과 평점 업데이트
        review.update(updateDto.getContent(), updateDto.getRating());

        Long likeCount = likeRepository.countByReview(review);

        // 현재 사용자의 좋아요 여부 확인
        boolean liked = likeRepository.existsByReviewAndUser(review, user);

        return ReviewResponseDto.from(review, likeCount, liked);
    }

    public ReviewResponseDto deleteReview(Long reviewId, String email) {
        Review review = reviewRepository.findById(reviewId).orElseThrow(()-> new IllegalArgumentException("리뷰를 찾을 수 없습니다."));
        User user = userRepository.findByEmail(email).orElseThrow(()-> new IllegalArgumentException("삭제 권한이 없습니다."));
        reviewRepository.delete(review);
        Long likeCount = likeRepository.countByReview(review);

        // 현재 사용자의 좋아요 여부 확인
        boolean liked = likeRepository.existsByReviewAndUser(review, user);
        return ReviewResponseDto.from(review, likeCount, liked);


    }

    private String uploadFile(MultipartFile file, String email) throws IOException {
        FileUtil.validateFile(file);

        var emailDirectory = Paths.get(fileDir, email);
        FileUtil.createDirectoryIfNotExists(emailDirectory);

        String newFileName = FileUtil.generateUniqueFileName(file.getOriginalFilename());
        Path destPath = emailDirectory.resolve(newFileName);

        try {
            Files.copy(file.getInputStream(), destPath);

            if (!Files.exists(destPath)) {
                throw new FileUploadException("파일 저장에 실패했습니다.");
            }

            return email + "/" + newFileName;

        } catch (IOException e) {
            try {
                FileUtil.deleteIfExists(destPath);
            } catch (IOException deleteError) {
                log.error("실패한 파일 삭제 중 오류 발생", deleteError);
            }
            throw new FileUploadException("파일 저장 중 오류가 발생했습니다.", e);
        }
    }

    @Transactional
    public void processImages(List<MultipartFile> images, Review review, String email) {
        if (images == null || images.isEmpty()) return;

        for (MultipartFile image : images) {
            try {
                String savedFileName = uploadFile(image, email);
                if (savedFileName != null) {
                    ReviewImage reviewImage = ReviewImage.builder()
                            .imageUrl(savedFileName)
                            .review(review)
                            .build();
                    review.addImage(reviewImage);
                }
            } catch (IOException e) {
                throw new FileUploadException("이미지 업로드 중 오류가 발생했습니다.", e);
            }
        }
    }

    @Transactional
    public void deleteImage(ReviewImage image) {
        try {
            var imagePath = Paths.get(fileDir, image.getImageUrl());
            FileUtil.deleteIfExists(imagePath);

            var parentDir = imagePath.getParent();
            if (Files.exists(parentDir) && Files.isDirectory(parentDir)) {
                try (var files = Files.list(parentDir)) {
                    if (files.count() == 0) {
                        FileUtil.deleteIfExists(parentDir);
                    }
                }
            }
        } catch (IOException e) {
            log.error("이미지 파일 삭제 중 오류 발생", e);
            throw new FileUploadException("이미지 삭제 중 오류가 발생했습니다.", e);
        }
    }
}

