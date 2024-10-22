package com.projectw.domain.review.controller;

import com.projectw.common.dto.SuccessResponse;
import com.projectw.domain.review.dto.request.ReviewRequestDto;
import com.projectw.domain.review.dto.request.ReviewUpdateDto;
import com.projectw.domain.review.dto.response.ReviewResponseDto;
import com.projectw.domain.review.service.ReviewService;
import com.projectw.security.AuthUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/reviews")
public class ReviewController {
    private final ReviewService reviewService;

    @PostMapping(value = "/review/{menuId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ReviewResponseDto> createReview(
            @PathVariable Long menuId,
            @ModelAttribute ReviewRequestDto reviewRequestDto,
            @RequestParam(value = "images", required = false) List<MultipartFile> images,
            @AuthenticationPrincipal AuthUser user
    ) {


        ReviewResponseDto responseDto = reviewService.createReview(
                menuId,
                reviewRequestDto,
                user.getEmail()
        );

        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }

    @GetMapping("/review/{menuId}")
    public ResponseEntity<List<ReviewResponseDto>> getMenuReviews(@PathVariable Long menuId) {
        List<ReviewResponseDto> reviews = reviewService.getMenuReviews(menuId);
        return ResponseEntity.ok(reviews);
    }

    @PutMapping(value = "/{reviewId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ReviewResponseDto> updateReview(
            @PathVariable Long reviewId,
            @Valid @ModelAttribute ReviewUpdateDto updateDto,
            @AuthenticationPrincipal AuthUser user
    ) {
        ReviewResponseDto responseDto = reviewService.updateReview(
                reviewId,
                updateDto,
                user.getEmail()
        );

        return ResponseEntity.ok(responseDto);
    }

    @DeleteMapping("review/{reviewId}")
    public ResponseEntity<ReviewResponseDto> deleteReview(@PathVariable Long reviewId,
                                                          @AuthenticationPrincipal AuthUser user) {
        String email = user.getEmail();
        return ResponseEntity.ok(reviewService.deleteReview(reviewId, email));
    }

}
