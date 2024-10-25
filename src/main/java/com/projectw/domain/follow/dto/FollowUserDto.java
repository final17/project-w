package com.projectw.domain.follow.dto;

import com.projectw.domain.user.entity.User;
import lombok.Getter;

@Getter
public class FollowUserDto {
    private Long userId;
    private String userNickname;

    public FollowUserDto(User user) {
        this.userId = user.getId();
        this.userNickname = user.getNickname();
    }
}
