package com.projectw.domain.review.controller;

import com.projectw.domain.review.dto.request.ReviewRequestDto;
import com.projectw.domain.review.dto.request.ReviewUpdateDto;
import com.projectw.domain.review.dto.response.ReviewResponseDto;
import com.projectw.domain.review.service.ReviewService;
import com.projectw.security.AuthUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/reviews")
public class ReviewController {
    private final ReviewService reviewService;

    @PostMapping(value = "{menuId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ReviewResponseDto> createReview(
            @PathVariable Long menuId,
            @ModelAttribute ReviewRequestDto reviewRequestDto,
            @RequestParam(value = "images", required = false) List<MultipartFile> images,
            @AuthenticationPrincipal AuthUser user
    ) {


        ReviewResponseDto responseDto = reviewService.createReview(
                menuId,
                reviewRequestDto,
                user.getEmail(),
                images
        );

        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }

    @GetMapping("/{menuId}")
    public ResponseEntity<Page<ReviewResponseDto>> getMenuReviews(
            @PathVariable Long menuId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ReviewResponseDto> reviews = reviewService.getMenuReviews(menuId, pageable);
        return ResponseEntity.ok(reviews);
    }

    @PutMapping(value = "/{reviewId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ReviewResponseDto> updateReview(
            @PathVariable Long reviewId,
            @Valid @ModelAttribute ReviewUpdateDto updateDto,
            @RequestParam(value = "newImages", required = false) List<MultipartFile> images,
            @AuthenticationPrincipal AuthUser user
    ) {
        ReviewResponseDto responseDto = reviewService.updateReview(
                reviewId,
                updateDto,
                user.getEmail()
        );

        return ResponseEntity.ok(responseDto);
    }

    @DeleteMapping("{reviewId}")
    public ResponseEntity<ReviewResponseDto> deleteReview(@PathVariable Long reviewId,
                                                          @AuthenticationPrincipal AuthUser user) {
        String email = user.getEmail();
        return ResponseEntity.ok(reviewService.deleteReview(reviewId, email));
    }

}
