package com.projectw.domain.dibs.service;

import com.projectw.common.enums.ResponseCode;
import com.projectw.common.enums.UserRole;
import com.projectw.common.exceptions.AccessDeniedException;
import com.projectw.domain.dibs.dto.request.DibsRequestDto;
import com.projectw.domain.dibs.dto.response.DibsActionResponseDto;
import com.projectw.domain.dibs.dto.response.DibsResponseDto;
import com.projectw.domain.dibs.entity.Dibs;
import com.projectw.domain.dibs.repository.DibsRepository;
import com.projectw.domain.follow.dto.FollowUserDto;
import com.projectw.domain.follow.service.FollowService;
import com.projectw.domain.store.entity.Store;
import com.projectw.domain.store.repository.StoreRepository;
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
public class DibsService {
    private final DibsRepository dibsRepository;
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final FollowService followService;


    @Transactional
    public DibsActionResponseDto addOrRemoveDibs(AuthUser authUser, DibsRequestDto requestDto) {
        // 로그인된 사용자인지 확인
        if (authUser == null || authUser.getRole() != UserRole.ROLE_USER) {
            throw new AccessDeniedException(ResponseCode.FORBIDDEN); // 로그인되지 않았거나 권한이 없는 경우 예외 발생
        }

        User user = userRepository.findById(authUser.getUserId())
                .orElseThrow(() -> new IllegalArgumentException(ResponseCode.NOT_FOUND_USER.getMessage()));

        Store store = storeRepository.findById(requestDto.getStoreId())
                .orElseThrow(() -> new IllegalArgumentException(ResponseCode.NOT_FOUND_STORE.getMessage()));

        // Dibs 존재 여부 확인
        Dibs existingDibs = dibsRepository.findByUserAndStore(user, store);

        if (existingDibs != null) {
            // 이미 찜한 가게인 경우 삭제
            dibsRepository.delete(existingDibs);
            return new DibsActionResponseDto("찜이 삭제되었습니다", null); // 삭제 메시지 반환
        } else {
            // 찜하지 않은 가게인 경우 새로 추가
            Dibs dibs = new Dibs(user, store);
            Dibs savedDibs = dibsRepository.save(dibs);
            return new DibsActionResponseDto("찜이 추가되었습니다", new DibsResponseDto(savedDibs));
        }
    }

    @Transactional(readOnly = true)
    public List<DibsResponseDto> getDibsList(AuthUser authUser) {
        // 로그인된 사용자인지 확인
        if (authUser == null || authUser.getRole() != UserRole.ROLE_USER) {
            throw new AccessDeniedException(ResponseCode.FORBIDDEN); // 로그인되지 않았거나 권한이 없는 경우 예외 발생
        }

        // User 조회 (인증된 사용자 기반)
        Long userId = authUser.getUserId();

        // Dibs 목록 조회
        List<Dibs> dibsList = dibsRepository.findByUserId(userId);

        // Dibs 리스트를 DibsResponseDto로 변환하여 반환
        return dibsList.stream()
                .map(DibsResponseDto::new)
                .collect(Collectors.toList());
    }

    // 사용자가 팔로우한 다른 사용자의 찜한 가게 목록 조회
    @Transactional(readOnly = true)
    public List<DibsResponseDto> getFollowingDibsList(AuthUser authUser) {
        // 사용자가 팔로우한 사용자 목록 조회 (List<FollowUserDto>로 반환됨)
        List<FollowUserDto> followingList = followService.getFollowingList(authUser);

        // 팔로우한 각 사용자의 Dibs 목록을 조회하고, DibsResponseDto로 변환
        return followingList.stream()
                .flatMap(followUserDto -> dibsRepository.findByUserId(followUserDto.getUserId()).stream())
                .map(DibsResponseDto::new)
                .collect(Collectors.toList());
    }
}
