package com.projectw.domain.store.service;


import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.projectw.common.config.S3Service;
import com.projectw.common.exceptions.AccessDeniedException;
import com.projectw.domain.category.DistrictCategory;
import com.projectw.domain.category.HierarchicalCategoryUtils;
import com.projectw.domain.menu.entity.Menu;
import com.projectw.domain.menu.repository.MenuRepository;
import com.projectw.domain.search.StoreDoc;
import com.projectw.domain.store.dto.StoreRequest;
import com.projectw.domain.store.dto.StoreResponse;
import com.projectw.domain.store.entity.Store;
import com.projectw.domain.store.repository.StoreRepository;
import com.projectw.domain.user.entity.User;
import com.projectw.domain.user.repository.UserRepository;
import com.projectw.security.AuthUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
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
    public StoreResponse.Info createStore(AuthUser authUser, StoreRequest.Create storeRequestDto, MultipartFile image) {

        User user = userRepository.findById(authUser.getUserId()).orElseThrow();

        DistrictCategory category = null;
        if(StringUtils.hasText(storeRequestDto.districtCategoryCode())){
            category = HierarchicalCategoryUtils.codeToCategory(DistrictCategory.class, storeRequestDto.districtCategoryCode());
        }


        String imageName = null;
        if (image != null) {
            imageName = s3Service.uploadFile(image);
        }

        Store newStore = Store.builder()
                .image(imageName)
                .title(storeRequestDto.title())
                .districtCategory(category)
                .description(storeRequestDto.description())
                .openTime(storeRequestDto.openTime())
                .lastOrder(storeRequestDto.lastOrder())
                .isNextDay(storeRequestDto.openTime().isAfter(storeRequestDto.lastOrder()))
                .closeTime(storeRequestDto.closeTime())
                .turnover(storeRequestDto.turnover())
                .reservationTableCount(storeRequestDto.reservationTableCount())
                .tableCount(storeRequestDto.tableCount())
                .phoneNumber(storeRequestDto.phoneNumber())
                .address(storeRequestDto.address())
                .deposit(storeRequestDto.deposit())
                .latitude(storeRequestDto.latitude())
                .longitude(storeRequestDto.longitude())
                .user(user)
                .reservations(null)
                .build();

        Store saveStore = storeRepository.save(newStore);
        createToElasticsearch(authUser, saveStore);
        return new StoreResponse.Info(saveStore);
    }


    @Override
    public StoreResponse.Info getOneStore(AuthUser authUser, Long storeId) {
        return new StoreResponse.Info(checkUserAndFindStore(authUser, storeId));
    }

    @Override
    @Transactional
    public StoreResponse.Info putStore(AuthUser authUser, Long storeId, StoreRequest.Create storeRequestDto, MultipartFile image) {
        Store findStore = checkUserAndFindStore(authUser, storeId);

        // 이미지가 있다면 기존 이미지를 삭제하고 새로운 이미지를 업로드합니다.
        String imageName = null;
        if (image != null) {
            s3Service.deleteFile(findStore.getImage());
            imageName = s3Service.uploadFile(image);
        }

        Store putStore = findStore.putStore(imageName, storeRequestDto);

        updateToElasticsearch(authUser, putStore);
        return new StoreResponse.Info(putStore);
    }

    /**
     * 가게 카테고리 변경
     */
    @Override
    @Transactional
    public StoreResponse.Info updateCategory(AuthUser authUser, Long storeId, StoreRequest.Category updateCategory) {
        Store store = checkUserAndFindStore(authUser, storeId);
        store.updateDistrictCategory(updateCategory.categoryCode());

        updateToElasticsearch(authUser, store);
        return new StoreResponse.Info(store);
    }

    @Override
    @Transactional
    public void deleteStore(AuthUser authUser, Long storeid) {
        Store findStore = checkUserAndFindStore(authUser, storeid);
        // 이미지 삭제
        deleteToElasticsearch(storeid);
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

    /**
     * 엘라스틱 서치에 도큐먼트 생성
     * @param authUser
     * @param store
     */
    private void createToElasticsearch(AuthUser authUser, Store store) {
        List<Menu> menus = menuRepository.findAllByStoreId(store.getId());
        StoreDoc doc = StoreDoc.of(store, User.fromAuthUser(authUser), menus);

        try {
            elasticClient.index(request -> request
                    .index("stores")
                    .id(String.valueOf(doc.getId()))
                    .document(doc));

        } catch (IOException e) {
            log.error("엘라스틱 서치 인덱싱 실패: {}", e.getMessage());
            throw new RuntimeException("");
        }
    }

    /**
     * 엘라스틱 서치에서 도큐먼트 삭제
     * @param storeId
     */
    private void deleteToElasticsearch(Long storeId) {
        try{
            // 엘라스틱 서치에서 도큐먼트 삭제처리
            elasticClient.delete(delete -> delete
                    .index("stores")
                    .id(String.valueOf(storeId)));
        } catch (IOException e) {
            log.error("store Id: {} 엘라스틱 서치에서 삭제 실패: {}", storeId, e.getMessage());
        }
    }

    /**
     * 엘라스틱 서치에서 도큐먼트 업데이트
     * @param authUser
     * @param store
     */
    private void updateToElasticsearch(AuthUser authUser, Store store) {
        List<Menu> menus = menuRepository.findAllByStoreId(store.getId());
        StoreDoc newDoc = StoreDoc.of(store, User.fromAuthUser(authUser), menus);
        try {
            elasticClient.update(update -> update
                    .index("stores")
                    .id(String.valueOf(newDoc.getId()))
                    .doc(newDoc), StoreDoc.class);
        } catch (IOException e) {
            log.error("엘라스틱 서치 인덱싱 실패: {}", e.getMessage());
            throw new RuntimeException("");
        }
    }

      // StoreOwnerServiceImpl 클래스
    @Override
    @Transactional(readOnly = true)
    public Page<StoreResponse.Info> getMyStores(AuthUser authUser, Pageable pageable) {
        User user = userRepository.findById(authUser.getUserId())
                .orElseThrow(() -> new AccessDeniedException(INVALID_USER_AUTHORITY));

        Page<Store> stores = storeRepository.findAllByUserId(user.getId(), pageable);
        return stores.map(StoreResponse.Info::new);
    }

}
