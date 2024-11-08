package com.projectw.domain.store.service;


import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.projectw.common.config.S3Service;
import com.projectw.common.exceptions.AccessDeniedException;
import com.projectw.domain.category.DistrictCategory;
import com.projectw.domain.category.HierarchicalCategory;
import com.projectw.domain.category.HierarchicalCategoryUtils;
import com.projectw.domain.menu.entity.Menu;
import com.projectw.domain.menu.repository.MenuRepository;
import com.projectw.domain.search.StoreDoc;
import com.projectw.domain.store.dto.request.StoreRequestDto;
import com.projectw.domain.store.dto.response.StoreResponseDto;
import com.projectw.domain.store.entity.Store;
import com.projectw.domain.store.repository.StoreRepository;
import com.projectw.domain.user.entity.User;
import com.projectw.domain.user.repository.UserRepository;
import com.projectw.security.AuthUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

import static com.projectw.common.enums.ResponseCode.INVALID_USER_AUTHORITY;
import static com.projectw.common.enums.ResponseCode.NOT_FOUND_STORE;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoreOwnerServiceImpl implements StoreOwnerService{

    private final StoreRepository storeRepository;
    private final UserRepository userRepository;

    private final ElasticsearchClient elasticClient;
    private final MenuRepository menuRepository;

    private final S3Service s3Service;


    @Override
    @Transactional
    public StoreResponseDto createStore(AuthUser authUser, StoreRequestDto storeRequestDto, MultipartFile image) {

        User user = userRepository.findById(authUser.getUserId()).orElseThrow();
        HierarchicalCategory category = HierarchicalCategoryUtils.codeToCategory(DistrictCategory.class, storeRequestDto.getDistrictCategoryCode());


        String imageName = null;
        if (image != null) {
            imageName = s3Service.uploadFile(image);
        }

        Store newStore = Store.builder()
                .image(imageName)
                .title(storeRequestDto.getTitle())
                .districtCategoryCode(category.getPath())
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
        List<Menu> menus = menuRepository.findAllByStoreId(saveStore.getId());
        StoreDoc doc = StoreDoc.of(saveStore, user, menus);

        // 엘라스틱 서치에 도큐먼트 등록
        try {
            elasticClient.index(request -> request
                    .index("stores")
                    .id(String.valueOf(doc.getId()))
                    .document(doc));

        } catch (IOException e) {
            log.error("엘라스틱 서치 인덱싱 실패: {}", e.getMessage());
            throw new RuntimeException("");
        }

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

        List<Menu> menus = menuRepository.findAllByStoreId(putStore.getId());
        StoreDoc newDoc = StoreDoc.of(putStore, User.fromAuthUser(authUser), menus);
        // 엘라스틱 서치에 도큐먼트 등록
        try {
            elasticClient.update(update -> update
                    .index("stores")
                    .id(String.valueOf(newDoc.getId()))
                    .doc(newDoc), StoreDoc.class);
        } catch (IOException e) {
            log.error("엘라스틱 서치 인덱싱 실패: {}", e.getMessage());
            throw new RuntimeException("");
        }
        return new StoreResponseDto(putStore);
    }

    @Override
    @Transactional
    public void deleteStore(AuthUser authUser, Long storeid) {
        Store findStore = checkUserAndFindStore(authUser, storeid);

        if(findStore == null) return;

        try{
            // 엘라스틱 서치에서 도큐먼트 삭제처리
            findStore.deleteStore();
            elasticClient.delete(delete -> delete
                    .index("stores")
                    .id(String.valueOf(storeid)));
        } catch (IOException e) {
            log.error("store Id: {} 엘라스틱 서치에서 삭제 실패: {}", storeid, e.getMessage());
        }

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
