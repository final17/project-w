package com.projectw.domain.menu.service;

import com.projectw.common.enums.ResponseCode;
import com.projectw.common.enums.UserRole;
import com.projectw.common.exceptions.AccessDeniedException;
import com.projectw.domain.allergy.entity.Allergy;
import com.projectw.domain.allergy.repository.AllergyRepository;
import com.projectw.domain.menu.dto.request.MenuRequestDto;
import com.projectw.domain.menu.dto.response.MenuResponseDto;
import com.projectw.domain.menu.entity.Menu;
import com.projectw.domain.menu.repository.MenuRepository;
import com.projectw.domain.store.entity.Store;
import com.projectw.domain.store.repository.StoreRepository;
import com.projectw.security.AuthUser;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class MenuService {

    private final MenuRepository menuRepository;
    private final StoreRepository storeRepository;
    private final AllergyRepository allergyRepository;
    private final RedissonClient redissonClient;

    // 메뉴 생성 (ROLE_OWNER 권한만 가능)
    public MenuResponseDto createMenu(AuthUser authUser, MenuRequestDto requestDto, Long storeId) throws IOException {
        // 사용자 권한 확인 (ROLE_OWNER만 메뉴 생성 가능)
        if (authUser.getRole() != UserRole.ROLE_OWNER) {
            throw new AccessDeniedException(ResponseCode.FORBIDDEN); // 권한 없을 때 예외 발생
        }

        // 스토어 조회
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException(ResponseCode.NOT_FOUND_STORE.getMessage()));

        // 알레르기 정보 가져오기
        Set<Allergy> allergies = allergyRepository.findAllById(requestDto.getAllergyIds())
                .stream().collect(Collectors.toSet());

        // 메뉴 생성 및 저장
        Menu menu = new Menu(
                requestDto.getName(),
                requestDto.getPrice(),
                store,
                allergies
        );
        Menu savedMenu = menuRepository.save(menu);

        return new MenuResponseDto(
                savedMenu.getId(),
                savedMenu.getName(),
                savedMenu.getPrice(),
                savedMenu.getAllergies().stream().map(Allergy::getName).collect(Collectors.toSet()),
                savedMenu.getViewCount()
        );
    }

    // 메뉴 수정 (ROLE_OWNER 권한만 가능)
    public MenuResponseDto updateMenu(AuthUser authUser, MenuRequestDto requestDto, Long storeId, Long menuId) throws IOException {
        // 사용자 권한 확인 (ROLE_OWNER만 메뉴 수정 가능)
        if (authUser.getRole() != UserRole.ROLE_OWNER) {
            throw new AccessDeniedException(ResponseCode.FORBIDDEN); // 권한 없을 때 예외 발생
        }

        // 메뉴 및 스토어 조회
        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new IllegalArgumentException(ResponseCode.NOT_FOUND_MENU.getMessage()));

        // 알레르기 정보 업데이트
        Set<Allergy> allergies = allergyRepository.findAllById(requestDto.getAllergyIds())
                .stream().collect(Collectors.toSet());
        menu.updateMenu(
                requestDto.getName(),
                requestDto.getPrice(),
                allergies
        );

        Menu updatedMenu = menuRepository.save(menu);

        return new MenuResponseDto(
                updatedMenu.getId(),
                updatedMenu.getName(),
                updatedMenu.getPrice(),
                updatedMenu.getAllergies().stream().map(Allergy::getName).collect(Collectors.toSet()),
                updatedMenu.getViewCount()
        );
    }

    // 모든 유저가 특정 가게의 메뉴 조회
    public List<MenuResponseDto> getMenusByStore(Long storeId) {
        // 스토어 조회
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 스토어를 찾을 수 없습니다."));

        // 해당 가게의 메뉴 조회 (isDeleted가 false인 메뉴만 조회)
        List<Menu> menus = menuRepository.findAllByStoreAndIsDeletedFalse(store);

        return menus.stream().map(menu -> new MenuResponseDto(
                menu.getId(),
                menu.getName(),
                menu.getPrice(),
                menu.getAllergies().stream().map(Allergy::getName).collect(Collectors.toSet()),
                menu.getViewCount()
        )).collect(Collectors.toList());
    }

    // 오너가 자신의 가게 메뉴만 조회
    public List<MenuResponseDto> getOwnerMenus(AuthUser authUser, Long storeId) {
        if (authUser.getRole() != UserRole.ROLE_OWNER) {
            throw new AccessDeniedException(ResponseCode.FORBIDDEN);
        }

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException(ResponseCode.NOT_FOUND_STORE.getMessage()));

        // 해당 스토어의 메뉴만 조회 (isDeleted가 false인 메뉴만)
        List<Menu> menus = menuRepository.findAllByStoreAndIsDeletedFalse(store);

        return menus.stream().map(menu -> new MenuResponseDto(
                menu.getId(),
                menu.getName(),
                menu.getPrice(),
                menu.getAllergies().stream().map(Allergy::getName).collect(Collectors.toSet()),
                menu.getViewCount()
        )).collect(Collectors.toList());
    }

    // 메뉴 삭제 (ROLE_OWNER 권한만 가능)
    public void deleteMenu(AuthUser authUser, Long storeId, Long menuId) {
        // 오너 권한 확인
        if (authUser.getRole() != UserRole.ROLE_OWNER) {
            throw new AccessDeniedException(ResponseCode.FORBIDDEN); // 권한 없음 예외 처리
        }

        // 스토어 및 메뉴 조회
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException(ResponseCode.NOT_FOUND_STORE.getMessage()));
        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new IllegalArgumentException(ResponseCode.NOT_FOUND_MENU.getMessage()));

        // 메뉴가 해당 스토어에 속해 있는지 확인
        if (!menu.getStore().getId().equals(store.getId())) {
            throw new AccessDeniedException(ResponseCode.FORBIDDEN); // 스토어 주인이 아닌 경우 예외 처리
        }

        // 메뉴 삭제
        menu.deleteMenu();
        menuRepository.save(menu);
    }

    // 메뉴 조회수 증가
    @Transactional
    public MenuResponseDto viewMenu(Long menuId) {
        String lockKey = "lock:menu:view:" + menuId; // 메뉴 ID 기반으로 고유 락 키 생성
        RLock lock = redissonClient.getLock(lockKey);

        try {
            // 락을 시도 (5초 이내에 락을 획득할 수 있어야 함, 획득 후 2초 동안 유지)
            if (lock.tryLock(5, 2, TimeUnit.SECONDS)) {
                Menu menu = menuRepository.findById(menuId)
                        .orElseThrow(() -> new IllegalArgumentException("메뉴를 찾을 수 없습니다."));
                menu.incrementViews();
                menuRepository.save(menu);

                return new MenuResponseDto(menu.getId(), menu.getName(), menu.getPrice(),
                        menu.getAllergies().stream().map(Allergy::getName).collect(Collectors.toSet()),
                        menu.getViewCount());
            } else {
                throw new RuntimeException("락을 획득할 수 없습니다. 잠시 후 다시 시도해 주세요.");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("락 획득 중 인터럽트가 발생했습니다.", e);
        } finally {
            // 락 해제
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}