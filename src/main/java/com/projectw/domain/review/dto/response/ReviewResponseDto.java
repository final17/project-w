package com.projectw.domain.review.dto.response;

import com.projectw.domain.menu.entity.Menu;
import com.projectw.domain.review.entity.Review;
import com.projectw.domain.review.entity.ReviewImage;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class ReviewResponseDto {
    private Long id;
    private String title;
    private String content;
    private List<String> imageUrls;
    private int rating;
    private String username;
    private String storeName;
    private String menuName;
    private Long likeCount;
    private boolean liked;
    private LocalDateTime reservationDate;
    private LocalDateTime createdAt;

    public static ReviewResponseDto from(Review review, Long likeCount, boolean liked) {
        return ReviewResponseDto.builder()
                .id(review.getId())
                .title(review.getTitle())
                .content(review.getContent())
                .rating(review.getRating())
                .username(review.getUser().getNickname())
                .storeName(review.getStore().getTitle())
                .menuName("") // 일단 빈 문자열로 설정
                .reservationDate(review.getReservation().getCreatedAt())
                .createdAt(review.getCreatedAt())
                .liked(liked)
                .likeCount(likeCount)
                .imageUrls(review.getImages().stream()
                        .map(ReviewImage::getImageUrl)
                        .collect(Collectors.toList()))
                .build();
    }

    // 메뉴 정보를 포함하는 새로운 팩토리 메서드
    public static ReviewResponseDto fromWithMenu(Review review, Long likeCount, boolean liked, Menu menu) {
        ReviewResponseDto dto = from(review, likeCount, liked);
        if (menu != null) {
            dto.menuName = menu.getName();
        }
        return dto;
    }

}
