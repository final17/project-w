package com.projectw.domain.follow.dto;

import com.projectw.domain.user.entity.User;

public sealed interface FollowResponseDto permits FollowResponseDto.FollowAdded, FollowResponseDto.FollowRemoved {

    /**
     * 팔로우가 추가
     */
    record FollowAdded(Long userId, String userNickname, String message) implements FollowResponseDto {
        public FollowAdded(User user) {
            this(user.getId(), user.getNickname(), "팔로우가 추가되었습니다.");
        }
    }

    /**
     * 팔로우가 취소
     */
    record FollowRemoved(Long userId, String userNickname, String message) implements FollowResponseDto {
        public FollowRemoved(User user) {
            this(user.getId(), user.getNickname(), "팔로우가 취소되었습니다.");
        }
    }
}