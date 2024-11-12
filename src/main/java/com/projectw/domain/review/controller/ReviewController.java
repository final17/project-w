package com.projectw.domain.review.controller;

import com.projectw.domain.review.dto.ReviewRequest;
import com.projectw.domain.review.dto.ReviewResponse;
import com.projectw.domain.review.service.ReviewServiceImpl;
import com.projectw.security.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    private final ReviewServiceImpl reviewService;

    @PostMapping(value = "/{storeId}/{menuId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ReviewResponse.Info> createReview(
            @PathVariable Long storeId,
            @PathVariable Long menuId,
            @ModelAttribute ReviewRequest.Create reviewRequest,  // ReviewRequest.Create 사용
            @RequestParam(value = "images", required = false) List<MultipartFile> images,
            @AuthenticationPrincipal AuthUser user
    ) {
        ReviewResponse.Info responseDto = reviewService.createReview(
                storeId,
                menuId,
                reviewRequest,
                user.getEmail(),
                images
        );

        return ResponseEntity.ok(responseDto);
    }

    // 특정 메뉴에 대한 리뷰 목록 조회 API
    @GetMapping("/{menuId}")
    public ResponseEntity<Page<ReviewResponse.Info>> getMenuReviews(
            @PathVariable Long menuId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ReviewResponse.Info> reviews = reviewService.getMenuReviews(menuId, pageable);
        return ResponseEntity.ok(reviews);
    }

    // 특정 스토어에 대한 리뷰 목록 조회 API
    @GetMapping("/store/{storeId}")
    public ResponseEntity<Page<ReviewResponse.Info>> getStoreReviews(
            @PathVariable Long storeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ReviewResponse.Info> reviews = reviewService.getStoreReviews(storeId, pageable);
        return ResponseEntity.ok(reviews);
    }

    // 리뷰 수정 API
    @PutMapping(value = "/{reviewId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ReviewResponse.Info> updateReview(
            @PathVariable Long reviewId,
            @ModelAttribute ReviewRequest.Update reviewRequest,  // ReviewRequest.Update 사용
            @RequestParam(value = "newImages", required = false) List<MultipartFile> images,
            @AuthenticationPrincipal AuthUser user
    ) {
        ReviewResponse.Info responseDto = reviewService.updateReview(
                reviewId,
                reviewRequest,
                user.getEmail(),
                images
        );
        return ResponseEntity.ok(responseDto);
    }

    // 리뷰 삭제 API
    @DeleteMapping("{reviewId}")
    public ResponseEntity<ReviewResponse.Info> deleteReview(@PathVariable Long reviewId,
                                                            @AuthenticationPrincipal AuthUser user) {
        String email = user.getEmail();
        return ResponseEntity.ok(reviewService.deleteReview(reviewId, email));
    }

    // 내 리뷰 목록 조회 API
    @GetMapping("/my")
    public ResponseEntity<Page<ReviewResponse.Info>> getUserReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal AuthUser user) {

        Pageable pageable = PageRequest.of(page, size);
        Page<ReviewResponse.Info> reviews = reviewService.getUserReviews(user.getEmail(), pageable);
        return ResponseEntity.ok(reviews);
    }

}
