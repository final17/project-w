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

    /**
     * 사용자 권한 확인
     */
    private void checkUserAccess(AuthUser authUser) {
        if (authUser == null || authUser.getRole() != UserRole.ROLE_USER) {
            throw new AccessDeniedException(ResponseCode.FORBIDDEN);
        }
    }

    /**
     * 팔로우 또는 언팔로우 처리
     */
    @Transactional
    public FollowResponseDto followOrUnfollow(AuthUser authUser, Long targetUserId) {
        checkUserAccess(authUser);

        Long userId = authUser.getUserId();

        // 자기 자신을 팔로우하려는 경우 예외 처리
        if (userId.equals(targetUserId)) {
            throw new IllegalArgumentException(ResponseCode.CANNOT_FOLLOW_SELF.getMessage());
        }

        // 사용자 조회
        User follower = findUserById(userId);
        User following = findUserById(targetUserId);

        // 팔로우 여부 확인 후 처리
        return followRepository.findByFollowerAndFollowing(follower, following)
                .map(existingFollow -> (FollowResponseDto) unfollow(existingFollow, following))
                .orElseGet(() -> follow(follower, following)); // 모든 경우 FollowResponseDto 반환
    }

    /**
     * 팔로우 처리
     */
    private FollowResponseDto follow(User follower, User following) {
        logger.info("팔로우 요청: follower={}, following={}", follower.getId(), following.getId());

        if (follower == null || following == null) {
            throw new IllegalArgumentException("follower 또는 following이 null입니다.");
        }

        // 중복 검증
        boolean exists = followRepository.findByFollowerAndFollowing(follower, following).isPresent();
        if (exists) {
            throw new IllegalArgumentException("이미 팔로우 관계가 존재합니다.");
        }

        followRepository.save(new Follow(follower, following));
        logger.info("팔로우 성공: follower={}, following={}", follower.getId(), following.getId());

        return new FollowResponseDto.FollowAdded(following);
    }

    /**
     * 언팔로우 처리
     */
    private FollowResponseDto unfollow(Follow existingFollow, User following) {
        logger.info("언팔로우 요청: follower={}, following={}", existingFollow.getFollower().getId(), following.getId());

        followRepository.delete(existingFollow);
        logger.info("언팔로우 성공: follower={}, following={}", existingFollow.getFollower().getId(), following.getId());

        return new FollowResponseDto.FollowRemoved(following);
    }

    /**
     * 사용자 ID로 사용자 조회
     */
    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(ResponseCode.NOT_FOUND_USER.getMessage()));
    }

    /**
     * 팔로우한 사용자 목록 조회
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "followingList", key = "#authUser.userId")
    public List<FollowUserDto.Basic> getFollowingList(AuthUser authUser) {
        checkUserAccess(authUser);

        User follower = findUserById(authUser.getUserId());

        return followRepository.findByFollower(follower).stream()
                .map(follow -> new FollowUserDto.Basic(follow.getFollowing()))
                .collect(Collectors.toList());
    }

    /**
     * 팔로워 목록 조회
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "followerList", key = "#authUser.userId")
    public List<FollowUserDto.Basic> getFollowerList(AuthUser authUser) {
        checkUserAccess(authUser);

        User following = findUserById(authUser.getUserId());
        logger.debug("팔로워 목록을 조회 중입니다: {}", following);

        List<Follow> follows = followRepository.findByFollowing(following);
        logger.debug("조회된 팔로우 데이터: {}", follows);

        return follows.stream()
                .map(follow -> new FollowUserDto.Basic(follow.getFollower()))
                .collect(Collectors.toList());
    }
}