package com.projectw.domain.dibs.service;

import com.projectw.common.enums.ResponseCode;
import com.projectw.common.enums.UserRole;
import com.projectw.common.exceptions.AccessDeniedException;
import com.projectw.common.exceptions.NotFoundException;
import com.projectw.domain.dibs.dto.request.DibsRequestDto;
import com.projectw.domain.dibs.dto.response.DibsActionResponseDto;
import com.projectw.domain.dibs.dto.response.DibsResponseDto;
import com.projectw.domain.dibs.entity.Dibs;
import com.projectw.domain.dibs.repository.DibsRepository;
import com.projectw.domain.store.entity.Store;
import com.projectw.domain.store.repository.StoreRepository;
import com.projectw.domain.user.entity.User;
import com.projectw.domain.user.repository.UserRepository;
import com.projectw.security.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
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

    // 응답 메시지 상수화
    private static final String DIBS_ADDED_MESSAGE = "찜이 추가되었습니다";
    private static final String DIBS_REMOVED_MESSAGE = "찜이 삭제되었습니다";

    // 권한 체크 메서드
    private void checkUserAccess(AuthUser authUser) {
        if (authUser == null || authUser.getRole() != UserRole.ROLE_USER) {
            throw new AccessDeniedException(ResponseCode.FORBIDDEN);
        }
    }

    @Transactional
    public DibsActionResponseDto addOrRemoveDibs(AuthUser authUser, DibsRequestDto requestDto) {
        checkUserAccess(authUser);

        // User 조회
        User user = userRepository.findById(authUser.getUserId())
                .orElseThrow(() -> new NotFoundException(ResponseCode.NOT_FOUND_USER));

        // Store 조회
        Store store = storeRepository.findById(requestDto.getStoreId())
                .orElseThrow(() -> new NotFoundException(ResponseCode.NOT_FOUND_STORE));

        // Dibs 존재 여부에 따라 추가 또는 삭제 처리
        boolean isDeleted = dibsRepository.deleteByUserAndStore(user, store) > 0;

        if (isDeleted) {
            return new DibsActionResponseDto(DIBS_REMOVED_MESSAGE, null);
        } else {
            Dibs dibs = dibsRepository.save(new Dibs(user, store));
            return new DibsActionResponseDto(DIBS_ADDED_MESSAGE, new DibsResponseDto(dibs));
        }
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "dibsList", key = "#authUser.userId")
    public List<DibsResponseDto> getDibsList(AuthUser authUser) {
        checkUserAccess(authUser);

        Long userId = authUser.getUserId();

        List<Dibs> dibsList = dibsRepository.findByUserId(userId);

        return dibsList.stream()
                .map(DibsResponseDto::new)
                .collect(Collectors.toList());
    }
}