package com.projectw.domain.review.repository;

import com.projectw.domain.menu.entity.Menu;
import com.projectw.domain.review.entity.Review;
import com.projectw.domain.store.entity.Store;
import com.projectw.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewRepository extends JpaRepository<Review, Long>, ReviewDslRepository{
    @Query("SELECT r, COUNT(l) " +
            "FROM Review r " +
            "JOIN FETCH r.user " +
            "LEFT JOIN Like l ON l.review = r " +
            "WHERE r.menu = :menu " +
            "GROUP BY r " +
            "ORDER BY r.createdAt DESC")
    Page<Object[]> findAllByMenuWithUserAndLikeCount(@Param("menu") Menu menu, Pageable pageable);



    @Query("SELECT COUNT(r) > 0 FROM Review r " +
            "WHERE r.user = :user " +
            "AND r.reservation.store = (SELECT m.store FROM Menu m WHERE m = :menu)")
    boolean existsByUserAndMenu(
            @Param("user") User user,
            @Param("menu") Menu menu
    );

    Page<Review> findAllByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    @Query("SELECT r, COUNT(l) " +
            "FROM Review r " +
            "JOIN FETCH r.user " +
            "JOIN FETCH r.reservation res " +
            "LEFT JOIN Like l ON l.review = r " +
            "WHERE res.store = :store " +  // 특정 스토어의 리뷰 조회 조건 추가
            "GROUP BY r " +
            "ORDER BY r.createdAt DESC")
    Page<Object[]> findAllByStoreWithUserAndLikeCount(@Param("store") Store store, Pageable pageable);

}
