package com.projectw.domain.store.service;

import com.projectw.common.enums.UserRole;
import com.projectw.domain.category.DistrictCategory;
import com.projectw.domain.store.dto.StoreResponse;
import com.projectw.domain.store.entity.Store;
import com.projectw.domain.store.entity.StoreLike;
import com.projectw.domain.store.repository.StoreLikeRepository;
import com.projectw.domain.store.repository.StoreRepository;
import com.projectw.domain.user.entity.User;
import com.projectw.domain.user.repository.UserRepository;
import com.projectw.security.AuthUser;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class StoreUserServiceImplTest {

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private StoreLikeRepository storeLikeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RedissonClient redissonClient;

    @InjectMocks
    private StoreUserServiceImpl storeUserService;


    @Nested
    class getAllStore {
        @Test
        void getAllStore_Success() {
            // given
            AuthUser authUser = new AuthUser(1L, "1@naver.com", UserRole.ROLE_OWNER);
            User user = new User(authUser.getUserId(), authUser.getEmail(), authUser.getRole());

            // Creating store instances using the builder
            Store store1 = Store.builder()
                    .image("image1.jpg")
                    .title("Store 1")
                    .districtCategory(DistrictCategory.SEOUL_JUNG)
                    .description("Description 1")
                    .openTime(LocalTime.of(9, 0))
                    .lastOrder(LocalTime.of(22, 0))
                    .isNextDay(false)
                    .closeTime(LocalTime.of(23, 0))
                    .turnover(LocalTime.of(0, 30))
                    .reservationTableCount(5L)
                    .tableCount(10L)
                    .phoneNumber("010-1234-5678")
                    .address("Address 1")
                    .deposit(10000L)
                    .latitude(37.5665)
                    .longitude(126.9780)
                    .user(user)
                    .reservations(null)
                    .build();

            Store store2 = Store.builder()
                    .image("image2.jpg")
                    .title("Store 2")
                    .districtCategory(null) // Null to test handling of districtCategory
                    .description("Description 2")
                    .openTime(LocalTime.of(10, 0))
                    .lastOrder(LocalTime.of(21, 0))
                    .isNextDay(true)
                    .closeTime(LocalTime.of(0, 0)) // Next day close time
                    .turnover(LocalTime.of(0, 45))
                    .reservationTableCount(8L)
                    .tableCount(15L)
                    .phoneNumber("010-5678-1234")
                    .address("Address 2")
                    .deposit(20000L)
                    .latitude(37.5675)
                    .longitude(126.9790)
                    .user(user)
                    .reservations(null)
                    .build();

            List<Store> storeList = List.of(store1, store2);
            Page<Store> storePage = new PageImpl<>(storeList);
            Pageable pageable = PageRequest.of(0, 10);

            given(storeRepository.findAll(pageable)).willReturn(storePage);

            // when
            Page<StoreResponse.Info> result = storeUserService.getAllStore(pageable);

            // then
            assertNotNull(result);
            assertEquals(2, result.getTotalElements());
            assertEquals("Store 1", result.getContent().get(0).title());
            assertEquals("Store 2", result.getContent().get(1).title());

            // Verify districtCategory handling
            assertNotNull(result.getContent().get(0).districtCategory());
            assertNull(result.getContent().get(1).districtCategory());
        }
    }


    @Nested
    class getOneStore {
        @Test
        void getOneStore_Success() throws InterruptedException {
            // given
            Long storeId = 1L;
            AuthUser authUser = new AuthUser(1L, "user@example.com", UserRole.ROLE_USER);

            Store store = Store.builder()
                    .image("test-image.jpg")
                    .title("Test Store")
                    .districtCategory(DistrictCategory.SEOUL_JUNG)
                    .description("This is a test store")
                    .openTime(LocalTime.of(9, 0))
                    .lastOrder(LocalTime.of(22, 0))
                    .isNextDay(false)
                    .closeTime(LocalTime.of(23, 0))
                    .turnover(LocalTime.of(0, 30))
                    .reservationTableCount(5L)
                    .tableCount(20L)
                    .phoneNumber("123-456-7890")
                    .address("123 Test Street")
                    .deposit(5000L)
                    .latitude(37.5665)
                    .longitude(126.9780)
                    .user(new User(authUser.getUserId(), authUser.getEmail(), authUser.getRole()))
                    .reservations(null)
                    .build();

            RLock rLock = Mockito.mock(RLock.class); // rLock을 명시적으로 모킹
            given(redissonClient.getLock(anyString())).willReturn(rLock); // 모든 키에 대해 rLock 반환
            given(redissonClient.getLock("store:view:" + storeId)).willReturn(rLock); // 특정 키에 대해 rLock 반환

            given(rLock.tryLock(10, 2, TimeUnit.SECONDS)).willReturn(true); // 락 획득 성공 시 true 반환
            given(storeRepository.findById(storeId)).willReturn(Optional.of(store)); // Store 반환

            // when
            StoreResponse.Info result = storeUserService.getOneStore(authUser, storeId);

            // then
            assertNotNull(result);
            assertEquals("Test Store", result.title());
            assertEquals("This is a test store", result.description());
            assertEquals(1, store.getView()); // Ensure view count increased

            // verify interactions
            verify(rLock).tryLock(10, 2, TimeUnit.SECONDS);
            verify(storeRepository).findById(storeId);
        }
    }

    @Nested
    class storeAddView{
        @Test
        void searchStoreName_Success() {
            // given
            AuthUser authUser = new AuthUser(1L, "user@example.com", UserRole.ROLE_USER);
            String storeName = "Test Store";
            Pageable pageable = PageRequest.of(0, 10);

            Store store1 = Store.builder()
                    .image("test-image1.jpg")
                    .title("Test Store 1")
                    .districtCategory(DistrictCategory.SEOUL_JUNG)
                    .description("Description 1")
                    .openTime(LocalTime.of(9, 0))
                    .lastOrder(LocalTime.of(22, 0))
                    .isNextDay(false)
                    .closeTime(LocalTime.of(23, 0))
                    .turnover(LocalTime.of(0, 30))
                    .reservationTableCount(5L)
                    .tableCount(20L)
                    .phoneNumber("123-456-7890")
                    .address("123 Test Street")
                    .deposit(5000L)
                    .latitude(37.5665)
                    .longitude(126.9780)
                    .user(new User(authUser.getUserId(), authUser.getEmail(), authUser.getRole()))
                    .reservations(null)
                    .build();

            Store store2 = Store.builder()
                    .image("test-image2.jpg")
                    .title("Test Store 2")
                    .districtCategory(DistrictCategory.SEOUL_GANGNAM)
                    .description("Description 2")
                    .openTime(LocalTime.of(10, 0))
                    .lastOrder(LocalTime.of(21, 0))
                    .isNextDay(false)
                    .closeTime(LocalTime.of(22, 0))
                    .turnover(LocalTime.of(0, 45))
                    .reservationTableCount(3L)
                    .tableCount(15L)
                    .phoneNumber("987-654-3210")
                    .address("456 Test Avenue")
                    .deposit(3000L)
                    .latitude(37.4981)
                    .longitude(127.0276)
                    .user(new User(authUser.getUserId(), authUser.getEmail(), authUser.getRole()))
                    .reservations(null)
                    .build();

            Page<Store> storePage = new PageImpl<>(List.of(store1, store2), pageable, 2);

            given(storeRepository.findAllByTitle(pageable, storeName)).willReturn(storePage);

            // when
            Page<StoreResponse.Info> result = storeUserService.searchStoreName(authUser, storeName, pageable);

            // then
            assertNotNull(result);
            assertEquals(2, result.getTotalElements());
            assertEquals("Test Store 1", result.getContent().get(0).title());
            assertEquals("Test Store 2", result.getContent().get(1).title());

            // verify interactions
            verify(storeRepository).findAllByTitle(pageable, storeName);
        }
    }

    @Test
    void getLikeStore_Success() {
        // given
        Long userId = 1L;
        AuthUser authUser = new AuthUser(userId, "user@example.com", UserRole.ROLE_USER);

        Pageable pageable = PageRequest.of(0, 5);
        Store store1 = Store.builder()
                .title("Store 1")
                .address("123 Test Street")
                .build();
        Store store2 = Store.builder()
                .title("Store 2")
                .address("456 Test Avenue")
                .build();
        StoreLike storeLike1 = new StoreLike(new User(userId, "user@example.com", UserRole.ROLE_USER), store1);
        StoreLike storeLike2 = new StoreLike(new User(userId, "user@example.com", UserRole.ROLE_USER), store2);

        Page<StoreLike> storeLikes = new PageImpl<>(List.of(storeLike1, storeLike2), pageable, 2);

        // Mock 설정
        given(storeLikeRepository.findAllByUserId(userId, pageable)).willReturn(storeLikes);

        // when
        Page<StoreResponse.Like> result = storeUserService.getLikeStore(authUser, pageable);

        // then
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getContent().size());

        StoreResponse.Like response1 = result.getContent().get(0);
        StoreResponse.Like response2 = result.getContent().get(1);

        assertEquals("Store 1", response1.storeName());
        assertEquals("Store 2", response2.storeName());

        // verify interactions
        verify(storeLikeRepository).findAllByUserId(userId, pageable);
    }

//    @Test
//    void searchStoreName() {
//    }


//    @Test
//    void likeStore() {
//    }


//    @Test
//    void increaseStoreLike() {
//    }


//    @Test
//    void getLikeStore() {
//    }
}