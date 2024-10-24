package com.projectw.domain.comment.entity;

import com.projectw.common.entity.Timestamped;
import com.projectw.domain.comment.dto.request.CommentRequestDto;
import com.projectw.domain.review.entity.Review;
import com.projectw.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "comments")
public class Comment extends Timestamped {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id")
    private Review review;

    public Comment(Review review, CommentRequestDto requestDto, User user) {
        this.id = requestDto.getId();
        this. review = review;
        this.user = user;
    }

    public void update(CommentRequestDto requestDto) {
        this.id = requestDto.getId();
        this.content = requestDto.getContent();
    }
}