package com.projectw.domain.store.service;


import com.projectw.common.config.S3Service;
import com.projectw.common.exceptions.AccessDeniedException;
import com.projectw.domain.store.dto.request.StoreRequestDto;
import com.projectw.domain.store.dto.response.StoreResponseDto;
import com.projectw.domain.store.entity.Store;
import com.projectw.domain.store.repository.StoreRepository;
import com.projectw.domain.user.entity.User;
import com.projectw.domain.user.repository.UserRepository;
import com.projectw.security.AuthUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static com.projectw.common.enums.ResponseCode.INVALID_USER_AUTHORITY;
import static com.projectw.common.enums.ResponseCode.NOT_FOUND_STORE;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoreOwnerServiceImpl implements StoreOwnerService{

    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final S3Service s3Service;

    @Override
    @Transactional
    public StoreResponseDto createStore(AuthUser authUser, StoreRequestDto storeRequestDto, MultipartFile image) {

        User user = userRepository.findById(authUser.getUserId()).orElseThrow();

        String imageName = null;
        if (image != null) {
            imageName = s3Service.uploadFile(image);
        }

        Store newStore = Store.builder()
                .image(imageName)
                .title(storeRequestDto.getTitle())
                .description(storeRequestDto.getDescription())
                .openTime(storeRequestDto.getOpenTime())
                .lastOrder(storeRequestDto.getLastOrder())
                .isNextDay(storeRequestDto.getOpenTime().isAfter(storeRequestDto.getLastOrder()))
                .closeTime(storeRequestDto.getCloseTime())
                .turnover(storeRequestDto.getTurnover())
                .reservationTableCount(storeRequestDto.getReservationTableCount())
                .tableCount(storeRequestDto.getTableCount())
                .phoneNumber(storeRequestDto.getPhoneNumber())
                .address(storeRequestDto.getAddress())
                .deposit(storeRequestDto.getDeposit())
                .latitude(storeRequestDto.getLatitude())
                .longitude(storeRequestDto.getLongitude())
                .user(user)
                .reservations(null)
                .build();

        Store saveStore = storeRepository.save(newStore);

        return new StoreResponseDto(saveStore);
    }

    @Override
    public StoreResponseDto getOneStore(AuthUser authUser, Long storeId) {
        return new StoreResponseDto(checkUserAndFindStore(authUser, storeId));
    }

    @Override
    @Transactional
    public StoreResponseDto putStore(AuthUser authUser, Long storeId, StoreRequestDto storeRequestDto, MultipartFile image) {
        Store findStore = checkUserAndFindStore(authUser, storeId);

        // 이미지가 있다면 기존 이미지를 삭제하고 새로운 이미지를 업로드합니다.
        String imageName = null;
        if (image != null) {
            s3Service.deleteFile(findStore.getImage());
            imageName = s3Service.uploadFile(image);
        }

        Store putStore = findStore.putStore(imageName, storeRequestDto);

        return new StoreResponseDto(putStore);
    }

    @Override
    @Transactional
    public void deleteStore(AuthUser authUser, Long storeid) {
        Store findStore = checkUserAndFindStore(authUser, storeid);
        // 이미지 삭제
        s3Service.deleteFile(findStore.getImage());
        findStore.deleteStore();
    }

    private Store checkUserAndFindStore(AuthUser authUser, Long storeId){
        User findUser = userRepository.findById(authUser.getUserId()).orElseThrow();

        Store findStore = storeRepository.findById(storeId).orElseThrow(() -> new AccessDeniedException(NOT_FOUND_STORE));

        if (!findStore.getUser().getEmail().equals(findUser.getEmail())) {
            throw new AccessDeniedException(INVALID_USER_AUTHORITY);
        }

        return findStore;
    }

}
