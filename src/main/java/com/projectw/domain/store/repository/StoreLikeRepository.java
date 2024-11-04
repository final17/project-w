package com.projectw.domain.store.repository;

import com.projectw.domain.store.entity.StoreLike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreLikeRepository extends JpaRepository<StoreLike, Long> {
    StoreLike findByIdAndUserId(Long findStore, Long findUser);
}
