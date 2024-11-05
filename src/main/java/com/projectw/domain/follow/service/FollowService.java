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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FollowService {

    private static final Logger logger = LoggerFactory.getLogger(FollowService.class);

    private final FollowRepository followRepository;
    private final UserRepository userRepository;

    // 권한 확인 메서드
    private void checkUserAccess(AuthUser authUser) {
        if (authUser == null || authUser.getRole() != UserRole.ROLE_USER) {
            throw new AccessDeniedException(ResponseCode.FORBIDDEN);
        }
    }

    @Transactional
    public FollowResponseDto followOrUnfollow(AuthUser authUser, Long targetUserId) {
        checkUserAccess(authUser);

        Long userId = authUser.getUserId();

        // 자기 자신을 팔로우하려는 경우 예외 발생
        if (userId.equals(targetUserId)) {
            throw new IllegalArgumentException(ResponseCode.CANNOT_FOLLOW_SELF.getMessage());
        }

        User follower = findUserById(userId);
        User following = findUserById(targetUserId);

        // 팔로우 여부에 따라 추가/삭제 처리 분리
        return followRepository.findByFollowerAndFollowing(follower, following)
                .map(existingFollow -> unfollow(existingFollow, following))
                .orElseGet(() -> follow(follower, following));
    }

    // 팔로우 메서드
    private FollowResponseDto follow(User follower, User following) {
        followRepository.save(new Follow(follower, following));
        return new FollowResponseDto(following, "팔로우가 추가되었습니다.");
    }

    // 언팔로우 메서드
    private FollowResponseDto unfollow(Follow existingFollow, User following) {
        followRepository.delete(existingFollow);
        return new FollowResponseDto(following, "팔로우가 취소되었습니다.");
    }

    // 사용자 조회 헬퍼 메서드
    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(ResponseCode.NOT_FOUND_USER.getMessage()));
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "followingList", key = "#authUser.userId")
    public List<FollowUserDto> getFollowingList(AuthUser authUser) {
        checkUserAccess(authUser);

        User follower = findUserById(authUser.getUserId());

        return followRepository.findByFollower(follower).stream()
                .map(follow -> new FollowUserDto(follow.getFollowing()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "followerList", key = "#authUser.userId")
    public List<FollowUserDto> getFollowerList(AuthUser authUser) {
        checkUserAccess(authUser);

        User following = findUserById(authUser.getUserId());

        return followRepository.findByFollowing(following).stream()
                .map(follow -> new FollowUserDto(follow.getFollower()))
                .collect(Collectors.toList());
    }
}