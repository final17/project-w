package com.projectw.domain.menu.repository;

import com.projectw.domain.menu.entity.Menu;
import com.projectw.domain.menu.entity.MenuLike;
import com.projectw.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MenuLikeRepository extends JpaRepository<MenuLike, Long> {
    Optional<MenuLike> findByMenuAndUser(Menu menu, User user);
    int countByMenu(Menu menu);
}