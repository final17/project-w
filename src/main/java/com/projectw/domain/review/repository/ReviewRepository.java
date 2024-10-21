package com.projectw.domain.review.repository;

import com.projectw.domain.comment.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Comment, Long> {
}
