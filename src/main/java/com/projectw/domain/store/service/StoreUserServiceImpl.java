package com.projectw.domain.store.service;

import com.projectw.domain.follow.dto.FollowUserDto;
import com.projectw.domain.follow.service.FollowService;
import com.projectw.domain.store.dto.StoreResponse;
import com.projectw.domain.store.entity.Store;
import com.projectw.domain.store.entity.StoreLike;
import com.projectw.domain.store.repository.StoreLikeRepository;
import com.projectw.domain.store.repository.StoreRepository;
import com.projectw.domain.user.entity.User;
import com.projectw.domain.user.repository.UserRepository;
import com.projectw.security.AuthUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class StoreUserServiceImpl implements StoreUserService {

    private final StoreRepository storeRepository;
    private final StoreLikeRepository storeLikeRepository;
    private final UserRepository userRepository;
    private final FollowService followService;

    // redis
    private final RedissonClient redissonClient;

    @Override
    public Page<StoreResponse.Info> getAllStore(AuthUser authUser, Pageable pageable) {
        Page<Store> allStore = storeRepository.findAll(pageable);
        return allStore.map(StoreResponse.Info::new);
    }

    @Override
    @Transactional
    public StoreResponse.Info getOneStore(AuthUser authUser, Long storeId) {
        String key = "store:view:" + storeId;
        RLock rLock = redissonClient.getLock(key);
        Store store = null;

        try{
            boolean available = rLock.tryLock(10, 2, TimeUnit.SECONDS);
            if (!available) {
                log.info("Lock 획득 실패 = " + key);
                throw new RuntimeException("Lock 획득 실패: 다른 프로세스에서 사용 중입니다.");
            }

            // 로직 실행
            store = storeAddView(storeId);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // 인터럽트 상태 복원
            log.error("락 획득 중 인터럽트 발생", e);
            throw new RuntimeException("락 획득 중 인터럽트 발생", e);
        } finally {
            if (rLock.isHeldByCurrentThread()) { // 현재 스레드가 락을 가지고 있는지 확인
                log.info("락 해제: " + key);
                rLock.unlock();
            } else {
                log.warn("이미 락이 해제되었습니다: " + key);
            }
        }

        return new StoreResponse.Info(store);
    }

    @Transactional
    public Store storeAddView(Long storeId) {
        Store findStore = storeRepository.findById(storeId).orElseThrow(()-> new IllegalArgumentException("음식점을 찾을 수 없습니다."));
        findStore.addView();
        return findStore;
    }

    @Override
    public Page<StoreResponse.Info> searchStoreName(AuthUser authUser, String storeName, Pageable pageable) {
        Page<Store> storeList = storeRepository.findAllByTitle(pageable, storeName);

        return storeList.map(StoreResponse.Info::new);
    }

    @Override
    public StoreResponse.Like likeStore(AuthUser authUser, Long storeId) {
        String key = "store:like:" + storeId;
        RLock rLock = redissonClient.getLock(key);
        StoreLike storeLike = null;

        try{
            boolean available = rLock.tryLock(10, 2, TimeUnit.SECONDS);
            if (!available) {
                log.info("Lock 획득 실패 = " + key);
                throw new RuntimeException("Lock 획득 실패: 다른 프로세스에서 사용 중입니다.");
            }

            // 로직 실행
            storeLike = increaseStoreLike(authUser, storeId);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // 인터럽트 상태 복원
            log.error("락 획득 중 인터럽트 발생", e);
            throw new RuntimeException("락 획득 중 인터럽트 발생", e);
        } finally {
            if (rLock.isHeldByCurrentThread()) { // 현재 스레드가 락을 가지고 있는지 확인
                log.info("락 해제: " + key);
                rLock.unlock();
            } else {
                log.warn("이미 락이 해제되었습니다: " + key);
            }
        }

        return new StoreResponse.Like(storeLike);
    }

    @Transactional
    public StoreLike increaseStoreLike(AuthUser authUser, Long storeId) {
        Store findStore = storeRepository.findById(storeId).orElseThrow(() -> new IllegalArgumentException("음식점을 찾을 수 없습니다."));
        User findUser = userRepository.findById(authUser.getUserId()).orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        StoreLike storeLike = storeLikeRepository.findByStoreIdAndUserId(findStore.getId(), findUser.getId());

        if (storeLike == null) {
            StoreLike newStoreLike = new StoreLike(findUser, findStore);
            storeLike = storeLikeRepository.save(newStoreLike);

        } else {
            storeLike.changeLike();
            storeLike = storeLikeRepository.save(storeLike);
        }

        return storeLike;
    }

    @Override
    public Page<StoreResponse.Like> getLikeStore(AuthUser authUser, Pageable pageable) {
        Page<StoreLike> storeLikes = storeLikeRepository.findAllByUserId(authUser.getUserId(), pageable);
        return storeLikes.map(StoreResponse.Like::new);
    }

    @Override
    public Page<StoreResponse.Like> getLikedStoresOfFollowedUsers(AuthUser authUser, Pageable pageable) {
        List<FollowUserDto.Basic> followedUsers = followService.getFollowingList(authUser);

        List<Long> followedUserIds = followedUsers.stream()
                .map(FollowUserDto.Basic::userId)
                .collect(Collectors.toList());

        if (followedUserIds.isEmpty()) {
            return Page.empty();
        }

        Page<StoreLike> likedStores = storeLikeRepository.findAllByUserIds(followedUserIds, pageable);

        return likedStores.map(StoreResponse.Like::new);
    }
}
