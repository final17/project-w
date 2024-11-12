package com.projectw.domain.follow;

import com.projectw.common.enums.ResponseCode;
import com.projectw.common.enums.UserRole;
import com.projectw.common.exceptions.AccessDeniedException;
import com.projectw.domain.follow.dto.FollowResponseDto;
import com.projectw.domain.follow.dto.FollowUserDto;
import com.projectw.domain.follow.entity.Follow;
import com.projectw.domain.follow.repository.FollowRepository;
import com.projectw.domain.follow.service.FollowService;
import com.projectw.domain.user.entity.User;
import com.projectw.domain.user.repository.UserRepository;
import com.projectw.security.AuthUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FollowServiceTest {

    @InjectMocks
    private FollowService followService;

    @Mock
    private FollowRepository followRepository;

    @Mock
    private UserRepository userRepository;

    private AuthUser authUser;
    private User follower;
    private User following;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // 테스트 데이터 준비
        authUser = new AuthUser(1L, "test@example.com", UserRole.ROLE_USER);
        follower = new User(1L, "follower@example.com", UserRole.ROLE_USER);
        following = new User(2L, "following@example.com", UserRole.ROLE_USER);
    }

    @Test
    void followOrUnfollow_followSuccess() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(follower));
        when(userRepository.findById(2L)).thenReturn(Optional.of(following));
        when(followRepository.findByFollowerAndFollowing(follower, following)).thenReturn(Optional.empty());

        FollowResponseDto response = followService.followOrUnfollow(authUser, 2L);

        assertTrue(response instanceof FollowResponseDto.FollowAdded);
        verify(followRepository, times(1)).save(any(Follow.class));
    }

    @Test
    void followOrUnfollow_unfollowSuccess() {
        Follow existingFollow = new Follow(follower, following);

        when(userRepository.findById(1L)).thenReturn(Optional.of(follower));
        when(userRepository.findById(2L)).thenReturn(Optional.of(following));
        when(followRepository.findByFollowerAndFollowing(follower, following)).thenReturn(Optional.of(existingFollow));

        FollowResponseDto response = followService.followOrUnfollow(authUser, 2L);

        assertTrue(response instanceof FollowResponseDto.FollowRemoved);
        verify(followRepository, times(1)).delete(existingFollow);
    }

    @Test
    void followOrUnfollow_followSelfThrowsException() {
        AuthUser authUser = new AuthUser(1L, "test@example.com", UserRole.ROLE_USER);

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                followService.followOrUnfollow(authUser, 1L)
        );
        assertEquals(ResponseCode.CANNOT_FOLLOW_SELF.getMessage(), exception.getMessage());
    }

    @Test
    void followOrUnfollow_accessDeniedThrowsException() {
        AuthUser invalidAuthUser = new AuthUser(1L, "test@example.com", UserRole.ROLE_ADMIN);

        Exception exception = assertThrows(AccessDeniedException.class, () ->
                followService.followOrUnfollow(invalidAuthUser, 2L)
        );
        assertEquals(ResponseCode.FORBIDDEN.getMessage(), exception.getMessage());
    }

    @Test
    void getFollowingList_success() {
        Follow follow = new Follow(follower, following);
        when(userRepository.findById(1L)).thenReturn(Optional.of(follower));
        when(followRepository.findByFollower(follower)).thenReturn(List.of(follow));

        List<FollowUserDto.Basic> result = followService.getFollowingList(authUser);

        assertEquals(following.getId(), result.get(0).userId());
        assertEquals(following.getNickname(), result.get(0).userNickname());
    }

    @Test
    void getFollowerList_success() {
        AuthUser authUser = new AuthUser(2L, "following@example.com", UserRole.ROLE_USER);

        when(userRepository.findById(2L)).thenReturn(Optional.of(following));
        Follow follow = new Follow(follower, following);
        when(followRepository.findByFollowing(following)).thenReturn(List.of(follow));

        List<FollowUserDto.Basic> result = followService.getFollowerList(authUser);

        assertEquals(1, result.size());
        assertEquals(follower.getId(), result.get(0).userId());
        assertEquals(follower.getNickname(), result.get(0).userNickname());
    }
}