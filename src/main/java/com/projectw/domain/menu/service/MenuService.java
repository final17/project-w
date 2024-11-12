package com.projectw.domain.menu.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.projectw.common.config.S3Service;
import com.projectw.common.enums.ResponseCode;
import com.projectw.common.enums.UserRole;
import com.projectw.common.exceptions.AccessDeniedException;
import com.projectw.common.exceptions.NotFoundException;
import com.projectw.domain.allergy.entity.Allergy;
import com.projectw.domain.allergy.repository.AllergyRepository;
import com.projectw.domain.menu.dto.request.MenuRequestDto;
import com.projectw.domain.menu.dto.response.MenuResponseDto;
import com.projectw.domain.menu.entity.Menu;
import com.projectw.domain.menu.repository.MenuRepository;
import com.projectw.domain.review.entity.Review;
import com.projectw.domain.review.entity.ReviewImage;
import com.projectw.domain.search.StoreDoc;
import com.projectw.domain.store.entity.Store;
import com.projectw.domain.store.repository.StoreRepository;
import com.projectw.domain.user.entity.User;
import com.projectw.security.AuthUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Slf4j
public class MenuService {

    private final MenuRepository menuRepository;
    private final StoreRepository storeRepository;
    private final AllergyRepository allergyRepository;
    private final RedissonClient redissonClient;
    private final ElasticsearchClient elasticsearchClient;
    private final S3Service s3Service;

    // 공통된 락 처리 메서드
    private <T> T executeWithLock(String lockKey, Supplier<T> action) {
        RLock lock = redissonClient.getLock(lockKey);
        try {
            if (lock.tryLock(5, 10, TimeUnit.SECONDS)) {
                return action.get();
            } else {
                throw new RuntimeException("Lock 획득에 실패했습니다. 잠시 후 다시 시도해주세요.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Lock 획득 중 인터럽트가 발생했습니다.", e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    // 권한 검사 메서드
    private void checkOwnerRoleAndOwnership(AuthUser authUser, Store store) {
        if (authUser.getRole() != UserRole.ROLE_OWNER) {
            throw new AccessDeniedException(ResponseCode.FORBIDDEN);
        }
        if (!store.getOwnerId().equals(authUser.getUserId())) {
            throw new AccessDeniedException(ResponseCode.FORBIDDEN);
        }
    }

    // 메뉴 생성 (ROLE_OWNER 권한만 가능)
    @Transactional
    public MenuResponseDto.Detail createMenu(AuthUser authUser, MenuRequestDto.Create requestDto, Long storeId) throws IOException {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new NotFoundException(ResponseCode.NOT_FOUND_STORE));
        checkOwnerRoleAndOwnership(authUser, store);

        Set<Allergy> allergies = allergyRepository.findAllById(requestDto.allergyIds())
                .stream().collect(Collectors.toSet());

        Menu menu = new Menu(requestDto.name(), requestDto.price(), store, allergies);

        // 이미지 처리
        if (requestDto.image() != null && !requestDto.image().isEmpty()) {
            String imageUrl = s3Service.uploadFile(requestDto.image());
            menu.updateImageUrl(imageUrl);
        }

        Menu savedMenu = menuRepository.save(menu);

        updateToElasticsearch(store, authUser);
        return createDetailResponseDto(savedMenu);
    }

    // 메뉴 수정 (ROLE_OWNER 권한만 가능)
    @Transactional
    public MenuResponseDto.Detail updateMenu(AuthUser authUser, MenuRequestDto.Update requestDto, Long storeId, Long menuId) throws IOException {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new NotFoundException(ResponseCode.NOT_FOUND_STORE));
        checkOwnerRoleAndOwnership(authUser, store);

        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new NotFoundException(ResponseCode.NOT_FOUND_MENU));

        Set<Allergy> allergies = allergyRepository.findAllById(requestDto.allergyIds())
                .stream().collect(Collectors.toSet());

        menu.updateMenu(requestDto.name(), requestDto.price(), allergies);

        // 이미지 처리
        if (requestDto.image() != null && !requestDto.image().isEmpty()) {
            // 기존 이미지가 있다면 삭제
            if (menu.getImageUrl() != null) {
                String oldImageFileName = menu.getImageUrl().substring(menu.getImageUrl().lastIndexOf("/") + 1);
                s3Service.deleteFile(oldImageFileName);
            }
            String imageUrl = s3Service.uploadFile(requestDto.image());
            menu.updateImageUrl(imageUrl);
        }

        Menu updatedMenu = menuRepository.save(menu);

        updateToElasticsearch(store, authUser);
        return createDetailResponseDto(updatedMenu);
    }

    // 특정 가게의 메뉴 조회 (모든 유저)
    public List<MenuResponseDto.Detail> getMenusByStore(Long storeId) {
        String lockKey = "lock:store:" + storeId;
        return executeWithLock(lockKey, () -> {
            Store store = storeRepository.findById(storeId)
                    .orElseThrow(() -> new NotFoundException(ResponseCode.NOT_FOUND_STORE));
            List<Menu> menus = menuRepository.findAllByStoreAndIsDeletedFalse(store);
            return menus.stream().map(this::createDetailResponseDto).collect(Collectors.toList());
        });
    }

    // 오너가 자신의 가게 메뉴 조회
    public List<MenuResponseDto.Detail> getOwnerMenus(AuthUser authUser, Long storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new NotFoundException(ResponseCode.NOT_FOUND_STORE));
        checkOwnerRoleAndOwnership(authUser, store);

        List<Menu> menus = menuRepository.findAllByStoreAndIsDeletedFalse(store);
        return menus.stream().map(this::createDetailResponseDto).collect(Collectors.toList());
    }

    // 메뉴 삭제 (ROLE_OWNER 권한만 가능함)
    public void deleteMenu(AuthUser authUser, Long storeId, Long menuId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new NotFoundException(ResponseCode.NOT_FOUND_STORE));
        checkOwnerRoleAndOwnership(authUser, store);

        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new NotFoundException(ResponseCode.NOT_FOUND_MENU));

        if (!menu.getStore().getId().equals(store.getId())) {
            throw new AccessDeniedException(ResponseCode.FORBIDDEN);
        }

        menu.deleteMenu();
        menuRepository.save(menu);

        updateToElasticsearch(store, authUser);
    }

    // 메뉴 단건 조회수 증가
    @Transactional
    public MenuResponseDto.Detail viewMenu(Long menuId) {
        String lockKey = "lock:menu:view:" + menuId;
        return executeWithLock(lockKey, () -> {
            Menu menu = menuRepository.findById(menuId)
                    .orElseThrow(() -> new NotFoundException(ResponseCode.NOT_FOUND_MENU));
            menu.incrementViews();
            menuRepository.save(menu);
            return createDetailResponseDto(menu);
        });
    }

    // MenuResponseDto.Detail 생성 헬퍼 메서드
    private MenuResponseDto.Detail createDetailResponseDto(Menu menu) {
        return new MenuResponseDto.Detail(
                menu.getId(),
                menu.getName(),
                menu.getPrice(),
                menu.getAllergies().stream().map(Allergy::getName).collect(Collectors.toSet()),
                menu.getViewCount(),
                menu.getImageUrl()
        );
    }

    /**
     * 엘라스틱 서치 index 업데이트
     * @param store
     * @param authUser
     */
    private void updateToElasticsearch(Store store, AuthUser authUser) {

        List<Menu> menus = menuRepository.findAllByStoreId(store.getId());
        StoreDoc doc = StoreDoc.of(store, User.fromAuthUser(authUser), menus);
        try {
            elasticsearchClient.update(update -> update
                    .index("stores")
                    .id(String.valueOf(store.getId()))
                    .doc(doc), StoreDoc.class);
        } catch (IOException e) {
            log.error("StoreId: {} 엘라스틱 서치 업데이트 실패: {}", store.getId(), e.getMessage());
        }
    }

    @Transactional
    public void processImages(List<MultipartFile> images, Review review) {
        if (images == null || images.isEmpty()) return;

        for (MultipartFile image : images) {
            String imageUrl = s3Service.uploadFile(image);
            ReviewImage reviewImage = ReviewImage.builder()
                    .imageUrl(imageUrl)
                    .review(review)
                    .build();
            review.addImage(reviewImage);
        }
    }
}