package com.projectw.domain.comment.repository;

import com.projectw.domain.comment.entity.Comment;
import com.projectw.domain.review.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    @Query("SELECT c FROM Comment c " +
            "JOIN FETCH c.user " +
            "WHERE c.review = :review " +
            "ORDER BY c.createdAt DESC")
    Page<Comment> findAllByReviewWithUser(@Param("review") Review review, Pageable pageable);
}
