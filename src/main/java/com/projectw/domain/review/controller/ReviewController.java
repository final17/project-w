package com.projectw.domain.review.controller;

import com.projectw.domain.review.dto.request.ReviewRequestDto;
import com.projectw.domain.review.dto.response.ReviewResponseDto;
import com.projectw.domain.review.service.ReviewService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/reviews")
public class ReviewController {
    private final ReviewService reviewService;

//    @PostMapping("/{menuId}")
//    ResponseEntity<ReviewResponseDto> createReview(@PathVariable Long menuId,
//                                                   @RequestBody ReviewRequestDto reviewRequestDto,
//                                                   HttpServletRequest request) {
//        String email = (String) request.getAttribute("email");
//        ResponseEntity.ok(reviewService.createReview(menuId, reviewRequestDto, email));
//    }
}
