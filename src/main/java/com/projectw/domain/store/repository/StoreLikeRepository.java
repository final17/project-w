package com.projectw.domain.store.repository;

import com.projectw.domain.store.entity.StoreLike;
import com.projectw.security.AuthUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface StoreLikeRepository extends JpaRepository<StoreLike, Long> {
    StoreLike findByStoreIdAndUserId(Long findStore, Long findUser);

    Page<StoreLike> findAllByUserId(Long user_id, Pageable pageable);
}
