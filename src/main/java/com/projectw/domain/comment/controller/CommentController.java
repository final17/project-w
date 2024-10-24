package com.projectw.domain.comment.controller;

import com.projectw.domain.comment.dto.request.CommentRequestDto;
import com.projectw.domain.comment.dto.response.CommentResponseDto;
import com.projectw.domain.comment.service.CommentService;
import com.projectw.security.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/comment")
public class CommentController {
    private final CommentService commentService;

    @PostMapping("/{reviewId}")
    public ResponseEntity<CommentResponseDto> createComment(@PathVariable Long reviewId, CommentRequestDto requestDto,
                                                            @AuthenticationPrincipal AuthUser user) {
        CommentResponseDto commentResponseDto = commentService.createComment(reviewId, requestDto, user.getEmail());
        return ResponseEntity.ok(commentResponseDto);
    }

    @GetMapping("/{reviewId}")
    public ResponseEntity<Page<CommentResponseDto>> getComments(
            @PathVariable Long reviewId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<CommentResponseDto> comments = commentService.getComments(reviewId, pageable);
        return ResponseEntity.ok(comments);
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<CommentResponseDto> updateComment(@PathVariable Long commentId,
                                                            @RequestBody CommentRequestDto requestDto,
                                                            @AuthenticationPrincipal AuthUser user) {
        return ResponseEntity.ok(commentService.updateComment(commentId, requestDto, user.getEmail()));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<CommentResponseDto> deleteComment(@PathVariable Long commentId,
                                                            @AuthenticationPrincipal AuthUser authUser) {
        return ResponseEntity.ok(commentService.deleteComment(commentId, authUser.getEmail()));
    }
}
