package com.projectw.domain.follow.dto;

import com.projectw.domain.user.entity.User;
import lombok.Getter;

@Getter
public class FollowResponseDto {
    private Long userId;       // 팔로우한 사용자 ID
    private String userNickname;    // 팔로우한 사용자 닉네임
    private String message;     // 팔로우 상태 메시지 ("팔로우가 추가되었습니다" 또는 "팔로우가 취소되었습니다")

    public FollowResponseDto(User user, String message) {
        this.userId = user.getId();
        this.userNickname = user.getNickname();
        this.message = message;
    }
}