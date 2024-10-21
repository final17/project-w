package com.projectw.domain.review.service;

import com.projectw.domain.review.dto.request.ReviewRequestDto;
import com.projectw.domain.review.dto.response.ReviewResponseDto;
import com.projectw.domain.review.repository.ReviewRepository;
import com.projectw.domain.user.entitiy.User;
import com.projectw.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.awt.*;

@Service
public class ReviewService {
    private ReviewRepository reviewRepository;
//    private MenuRepository menuRepository;
//
//    public ReviewResponseDto createReview(Long menuId, ReviewRequestDto reviewRequestDto, String email) {
//        Menu menu = menuRepository.findById(menuId).orElseThrow(()-> new IllegalArgumentException("메뉴를 찾을 수 없습니다."));
//    }
}
