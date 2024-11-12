package com.projectw.domain.review.service;

import com.projectw.domain.review.dto.ReviewRequest;
import com.projectw.domain.review.dto.ReviewResponse;
import com.projectw.domain.review.entity.ReviewImage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ReviewService {
    public ReviewResponse.Info createReview(Long storeId, Long menuId, ReviewRequest.Create reviewRequestDto, String email, List<MultipartFile> images);

    public Page<ReviewResponse.Info> getMenuReviews(Long menuId, Pageable pageable);

    public ReviewResponse.Info updateReview(Long reviewId, ReviewRequest.Update updateDto, String email, List<MultipartFile> images);

    public ReviewResponse.Info deleteReview(Long reviewId, String email);

    public Page<ReviewResponse.Info> getUserReviews(String email, Pageable pageable);

    public Page<ReviewResponse.Info> getStoreReviews(Long storeId, Pageable pageable);
    }
