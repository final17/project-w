package com.projectw.domain.menu.repository;

import com.projectw.domain.like.entity.Like;
import com.projectw.domain.menu.entity.Menu;
import com.projectw.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MenuLikeRepository extends JpaRepository<Like, Long> {
    // 특정 메뉴와 사용자를 기반으로 MenuLike 객체 조회
    Optional<Like> findByMenuAndUser(Menu menu, User user);
    // 특정 메뉴의 좋아요 수 조회
    long countByMenu(Menu menu);
    // 특정 메뉴에 특정 사용자가 좋아요를 눌렀는지 확인
    boolean existsByMenuAndUser(Menu menu, User user);
}