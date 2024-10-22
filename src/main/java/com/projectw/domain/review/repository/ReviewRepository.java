package com.projectw.domain.review.repository;

import com.projectw.domain.menu.entity.Menu;
import com.projectw.domain.reservation.entity.Reservation;
import com.projectw.domain.review.entity.Review;
import com.projectw.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long>, ReviewDslRepository{
    @Query("SELECT r FROM Review r " +
            "JOIN FETCH r.user " +
            "JOIN FETCH r.reservation res " +
            "WHERE res.store = (SELECT m.store FROM Menu m WHERE m = :menu) " +
            "ORDER BY r.createdAt DESC")
    List<Review> findAllByMenuWithUser(@Param("menu") Menu menu);

    @Query("SELECT COUNT(r) > 0 FROM Review r " +
            "WHERE r.user = :user " +
            "AND r.reservation.store = (SELECT m.store FROM Menu m WHERE m = :menu)")
    boolean existsByUserAndMenu(
            @Param("user") User user,
            @Param("menu") Menu menu
    );
}
