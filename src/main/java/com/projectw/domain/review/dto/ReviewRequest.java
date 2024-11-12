package com.projectw.domain.review.dto;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

// Create와 Update 요청 모두에 적용될 sealed 인터페이스
public sealed interface ReviewRequest permits ReviewRequest.Create, ReviewRequest.Update {

    // Create 요청 DTO
    record Create(String title, String content, int rating) implements ReviewRequest {
    }

    // Update 요청 DTO
    record Update(String content, int rating, List<MultipartFile> newImages, List<Long> deleteImageIds) implements ReviewRequest {

    }
}