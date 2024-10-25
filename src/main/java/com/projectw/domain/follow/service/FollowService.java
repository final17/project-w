package com.projectw.domain.follow.service;

import com.projectw.common.enums.ResponseCode;
import com.projectw.common.enums.UserRole;
import com.projectw.common.exceptions.AccessDeniedException;
import com.projectw.domain.follow.dto.FollowResponseDto;
import com.projectw.domain.follow.dto.FollowUserDto;
import com.projectw.domain.follow.entity.Follow;
import com.projectw.domain.follow.repository.FollowRepository;
import com.projectw.domain.user.entity.User;
import com.projectw.domain.user.repository.UserRepository;
import com.projectw.security.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;

    @Transactional
    public FollowResponseDto followOrUnfollow(AuthUser authUser, Long targetUserId) {
        // 로그인된 사용자 여부 및 권한 확인
        if (authUser == null || authUser.getRole() != UserRole.ROLE_USER) {
            throw new AccessDeniedException(ResponseCode.FORBIDDEN);
        }

        Long userId = authUser.getUserId();

        // 자기 자신을 팔로우하려는 경우 예외 발생
        if (userId.equals(targetUserId)) {
            throw new IllegalArgumentException(ResponseCode.CANNOT_FOLLOW_SELF.getMessage());
        }

        User follower = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(ResponseCode.NOT_FOUND_USER.getMessage()));
        User following = userRepository.findById(targetUserId)
                .orElseThrow(() -> new IllegalArgumentException(ResponseCode.NOT_FOUND_USER.getMessage()));

        // 팔로우 여부 확인 및 추가/삭제 처리
        return followRepository.findByFollowerAndFollowing(follower, following)
                .map(existingFollow -> {
                    followRepository.delete(existingFollow);
                    return new FollowResponseDto(following, "팔로우가 취소되었습니다.");
                })
                .orElseGet(() -> {
                    followRepository.save(new Follow(follower, following));
                    return new FollowResponseDto(following, "팔로우가 추가되었습니다.");
                });
    }

    @Transactional(readOnly = true)
    public List<FollowUserDto> getFollowingList(AuthUser authUser) {
        if (authUser == null || authUser.getRole() != UserRole.ROLE_USER) {
            throw new AccessDeniedException(ResponseCode.FORBIDDEN);
        }

        User follower = userRepository.findById(authUser.getUserId())
                .orElseThrow(() -> new IllegalArgumentException(ResponseCode.NOT_FOUND_USER.getMessage()));

        return followRepository.findByFollower(follower).stream()
                .map(follow -> new FollowUserDto(follow.getFollowing()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<FollowUserDto> getFollowerList(AuthUser authUser) {
        if (authUser == null || authUser.getRole() != UserRole.ROLE_USER) {
            throw new AccessDeniedException(ResponseCode.FORBIDDEN);
        }

        User following = userRepository.findById(authUser.getUserId())
                .orElseThrow(() -> new IllegalArgumentException(ResponseCode.NOT_FOUND_USER.getMessage()));

        return followRepository.findByFollowing(following).stream()
                .map(follow -> new FollowUserDto(follow.getFollower()))
                .collect(Collectors.toList());
    }
}