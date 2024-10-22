package com.projectw.domain.comment.dto.response;

import com.projectw.domain.comment.dto.request.CommentRequestDto;
import com.projectw.domain.comment.entity.Comment;
import com.projectw.domain.user.entity.User;
import lombok.Getter;

@Getter
public class CommentResponseDto {
    private Long id;
    private String content;
    private User user;

    public CommentResponseDto(Comment comment) {
        this.id = comment.getId();
        this.content = comment.getContent();
        this.user = comment.getUser();
    }

    public CommentResponseDto(CommentRequestDto requestDto, User user) {
        this.id = requestDto.getId();
        this.content = requestDto.getContent();
        this.user = user;
    }

    public CommentResponseDto(Comment comment, User user) {
        this.id = comment.getId();
        this.content = comment.getContent();
        this.user = user;

    }
}
