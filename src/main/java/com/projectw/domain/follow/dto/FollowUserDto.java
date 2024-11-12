package com.projectw.domain.follow.dto;

import com.projectw.domain.user.entity.User;

public sealed interface FollowUserDto permits FollowUserDto.Basic {

    /**
     * 기본 팔로우 사용자 정보
     */
    record Basic(Long userId, String userNickname) implements FollowUserDto {
        public Basic(User user) {
            this(user.getId(), user.getNickname());
        }
    }
}