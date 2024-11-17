package com.projectw.domain.store.repository;

import com.projectw.domain.store.entity.StoreLike;
import com.projectw.security.AuthUser;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface StoreLikeRepository extends JpaRepository<StoreLike, Long> {
    StoreLike findByStoreIdAndUserId(Long findStore, Long findUser);

    Page<StoreLike> findAllByUserId(Long user_id, Pageable pageable);

    @Query("SELECT sl FROM StoreLike sl WHERE sl.user.id IN :userIds")
    Page<StoreLike> findAllByUserIds(@Param("userIds") List<Long> userIds, Pageable pageable);
}
