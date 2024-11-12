package com.projectw.domain.follow.repository;

import com.projectw.domain.follow.entity.Follow;
import com.projectw.domain.store.entity.StoreLike;
import com.projectw.domain.user.entity.User;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {

    Optional<Follow> findByFollowerAndFollowing(User follower, User following); // 팔로우 여부 확인
    List<Follow> findByFollower(User follower); // 사용자가 팔로우한 사용자 목록 조회
    List<Follow> findByFollowing(User following); // 사용자를 팔로잉하는 사용자 목록 조회
    @Query("SELECT sl FROM StoreLike sl WHERE sl.user.id IN :userIds")
    Page<StoreLike> findAllByUserIds(@Param("userIds") List<Long> userIds, Pageable pageable);
}