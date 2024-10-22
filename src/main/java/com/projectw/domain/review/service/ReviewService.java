package com.projectw.domain.review.service;

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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final MenuRepository menuRepository;
    private final UserRepository userRepository;
    private final ReservationRepository reservationRepository;

    @Value("${DB_DIR}")
    private String fileDir;

    @Transactional
    public ReviewResponseDto createReview(Long menuId, ReviewRequestDto reviewRequestDto, String email) {
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
                .content(reviewRequestDto.getContent())
                .rating(reviewRequestDto.getRating())
                .reservation(reservation)
                .build();

        // 이미지 업로드 처리
        if (reviewRequestDto.getImages() != null && !reviewRequestDto.getImages().isEmpty()) {
            for (MultipartFile image : reviewRequestDto.getImages()) {
                try {
                    String savedFileName = uploadFile(image);
                    if (savedFileName != null) {
                        ReviewImage reviewImage = ReviewImage.builder()
                                .imageUrl(savedFileName)
                                .review(review)
                                .build();
                        review.addImage(reviewImage);
                    }
                } catch (IOException e) {
                    throw new RuntimeException("이미지 업로드에 실패했습니다: " + e.getMessage());
                }
            }
        }

        Review savedReview = reviewRepository.save(review);
        return ReviewResponseDto.from(savedReview);
    }

    public List<ReviewResponseDto> getMenuReviews(Long menuId) {
        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new IllegalArgumentException("메뉴를 찾을 수 없습니다."));

        return reviewRepository.findAllByMenuWithUser(menu).stream()
                .map(ReviewResponseDto::from)
                .collect(Collectors.toList());
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
                    String savedFileName = uploadFile(image);
                    if (savedFileName != null) {
                        ReviewImage reviewImage = ReviewImage.builder()
                                .imageUrl(savedFileName)
                                .review(review)
                                .build();
                        review.addImage(reviewImage);
                    }
                } catch (IOException e) {
                    throw new RuntimeException("이미지 업로드에 실패했습니다: " + e.getMessage());
                }
            }
        }

        // 리뷰 내용과 평점 업데이트
        review.update(updateDto.getContent(), updateDto.getRating());

        return ReviewResponseDto.from(review);
    }

    public ReviewResponseDto deleteReview(Long reviewId, String email) {
        Review review = reviewRepository.findById(reviewId).orElseThrow(()-> new IllegalArgumentException("리뷰를 찾을 수 없습니다."));
        userRepository.findByEmail(email).orElseThrow(()-> new IllegalArgumentException("삭제 권한이 없습니다."));
        reviewRepository.delete(review);
        return ReviewResponseDto.from(review);


    }

    public String uploadFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return null;
        }

        String originalName = file.getOriginalFilename();
        String extension = originalName.substring(originalName.lastIndexOf("."));
        String savedFileName = UUID.randomUUID().toString() + extension;
        String savedPath = fileDir + savedFileName;

        file.transferTo(new File(savedPath));

        return savedFileName;  // DB에는 파일명만 저장
    }
}

