package com.projectw.domain.comment.service;

import com.projectw.domain.comment.dto.request.CommentRequestDto;
import com.projectw.domain.comment.dto.response.CommentResponseDto;
import com.projectw.domain.comment.entity.Comment;
import com.projectw.domain.comment.repository.CommentRepository;
import com.projectw.domain.review.entity.Review;
import com.projectw.domain.review.repository.ReviewRepository;
import com.projectw.domain.user.entity.User;
import com.projectw.domain.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;

    @Transactional
    public CommentResponseDto createComment(Long reviewId, CommentRequestDto requestDto, String email) {
        Review review = reviewRepository.findById(reviewId).orElseThrow(()-> new IllegalArgumentException("리뷰를 찾을 수 없습니다."));
        User user = userRepository.findByEmail(email).orElseThrow(()-> new IllegalArgumentException("권한이 없습니다."));
        Comment savedComment = new Comment(review, requestDto, user);
        commentRepository.save(savedComment);
        return new CommentResponseDto(savedComment);
    }

    public Page<CommentResponseDto> getComments(Long reviewId, Pageable pageable) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다."));

        return commentRepository.findAllByReviewWithUser(review, pageable)
                .map(CommentResponseDto::new);
    }


    @Transactional
    public CommentResponseDto updateComment(Long commentId, CommentRequestDto requestDto, String email) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(()-> new IllegalArgumentException("대댓글을 찾을 수 없습니다."));
        User user = userRepository.findByEmail(email).orElseThrow(()-> new IllegalArgumentException("권한이 없습니다."));
        comment.update(requestDto);
        return new CommentResponseDto(requestDto, user);
    }

    public CommentResponseDto deleteComment(Long commentId, String email) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(()-> new IllegalArgumentException("대댓글을 찾을 수 없습니다."));
        User user = userRepository.findByEmail(email).orElseThrow(()-> new IllegalArgumentException("권한이 없습니다."));
        commentRepository.delete(comment);
        return new CommentResponseDto(comment, user);
    }
}
