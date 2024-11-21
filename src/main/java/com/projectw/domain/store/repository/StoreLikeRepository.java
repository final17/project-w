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

    @Query("SELECT sl FROM StoreLike sl WHERE sl.user.id = :user_id and sl.storeLike=true")
    Page<StoreLike> findAllByUserId(@Param("user_id")Long user_id, Pageable pageable);

    @Query("SELECT sl FROM StoreLike sl WHERE sl.user.id IN :userIds")
    Page<StoreLike> findAllByUserIds(@Param("userIds") List<Long> userIds, Pageable pageable);

    @Query("SELECT count(sl) FROM StoreLike sl where sl.store.id = :storeId and sl.storeLike = true")
    Long findByStoreId(@Param("storeId") Long storeId);
}
