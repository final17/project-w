package com.projectw.domain.store.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.projectw.common.config.S3Service;
import com.projectw.common.enums.ResponseCode;
import com.projectw.common.enums.UserRole;
import com.projectw.common.exceptions.AccessDeniedException;
import com.projectw.domain.category.DistrictCategory;
import com.projectw.domain.menu.repository.MenuRepository;
import com.projectw.domain.store.dto.StoreRequest;
import com.projectw.domain.store.dto.StoreResponse;
import com.projectw.domain.store.entity.Store;
import com.projectw.domain.store.repository.StoreRepository;
import com.projectw.domain.user.entity.User;
import com.projectw.domain.user.repository.UserRepository;
import com.projectw.security.AuthUser;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StoreOwnerServiceImplTest {

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ElasticsearchClient elasticClient;

    @Mock
    private MenuRepository menuRepository;

    @Mock
    private S3Service s3Service;

    @InjectMocks
    private StoreOwnerServiceImpl storeOwnerService;

    @Test
    void createStore() {
        // given
        AuthUser authUser = new AuthUser(1L, "1@naver.com", UserRole.ROLE_OWNER);
        User user = new User(authUser.getUserId(), authUser.getEmail(), authUser.getRole());
        given(userRepository.findById(authUser.getUserId())).willReturn(Optional.of(user));

        StoreRequest.Create storeRequestDto = new StoreRequest.Create(
                "Test Store",              // title
                "R012",           // districtCategoryCode
                "This is a test store",    // description
                LocalTime.of(9, 0),        // openTime
                LocalTime.of(22, 0),       // lastOrder
                LocalTime.of(23, 0),       // closeTime
                LocalTime.of(0, 30),       // turnover
                5L,                        // reservationTableCount
                20L,                       // tableCount
                "123-456-7890",            // phoneNumber
                "123 Test Street",         // address
                5000L,                     // deposit
                37.5665,                   // latitude
                126.9780                   // longitude
        );

        MockMultipartFile image = new MockMultipartFile(
                "image",                 // 필드 이름
                "test-image.jpg",         // 파일 이름
                "image/jpeg",             // MIME 타입
                "test image content".getBytes() // 파일 내용 (바이트 배열)
        );

        DistrictCategory category = null;

        // Store 객체 생성
        Store newStore = Store.builder()
                .image(image.getName())
                .title(storeRequestDto.title())
                .districtCategory(DistrictCategory.SEOUL_JUNG) // 테스트에서는 생략
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

        ReflectionTestUtils.setField(newStore, "id", 1L);

        // Stubbing uploadFile 메서드
        given(s3Service.uploadFile(image)).willReturn("image");

        // Stubbing save 메서드
        given(storeRepository.save(any(Store.class))).willReturn(newStore);

        // when
        StoreResponse.Info response = storeOwnerService.createStore(authUser, storeRequestDto, image);

        // then
        assertNotNull(response);
        assertEquals(newStore.getTitle(), response.title());
        assertEquals(image.getName(), response.image());

        verify(userRepository, times(1)).findById(authUser.getUserId());
        verify(s3Service, times(1)).uploadFile(image);
        verify(storeRepository, times(1)).save(any(Store.class));
    }

    @Nested
    class getOneStore {
        @Test
        void getOneStore_withValidAuthUserAndStoreId_returnsStoreInfo() {
            // given
            Long storeId = 1L;
            AuthUser authUser = new AuthUser(1L, "1@naver.com", UserRole.ROLE_OWNER);
            User user = new User(authUser.getUserId(), authUser.getEmail(), authUser.getRole());

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
                    .user(user)
                    .reservations(null)
                    .build();

            ReflectionTestUtils.setField(store, "id", 1L);

            given(userRepository.findById(authUser.getUserId())).willReturn(Optional.of(user));
            given(storeRepository.findById(storeId)).willReturn(Optional.of(store));

            // when
            StoreResponse.Info response = storeOwnerService.getOneStore(authUser, storeId);

            // then
            assertNotNull(response);
            assertEquals(store.getTitle(), response.title());
            assertEquals(store.getImage(), response.image());
            verify(userRepository, times(1)).findById(authUser.getUserId());
            verify(storeRepository, times(1)).findById(storeId);
        }

        @Test
        void getOneStore_withInvalidUser_throwsAccessDeniedException() {
            // given
            Long storeId = 1L;
            AuthUser authUser = new AuthUser(1L, "1@naver.com", UserRole.ROLE_OWNER);
            User user = new User(authUser.getUserId(), authUser.getEmail(), authUser.getRole());

            // Store owned by a different user
            User differentUser = new User(2L, "different@naver.com", UserRole.ROLE_OWNER);
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
                    .user(differentUser)
                    .reservations(null)
                    .build();

            ReflectionTestUtils.setField(store, "id", 1L);

            given(userRepository.findById(authUser.getUserId())).willReturn(Optional.of(user));
            given(storeRepository.findById(storeId)).willReturn(Optional.of(store));

            // when & then
            AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                    () -> storeOwnerService.getOneStore(authUser, storeId));

            assertEquals(ResponseCode.INVALID_USER_AUTHORITY.getMessage(), exception.getMessage());
            verify(userRepository, times(1)).findById(authUser.getUserId());
            verify(storeRepository, times(1)).findById(storeId);
        }

        @Test
        void getOneStore_withNonExistentStore_throwsAccessDeniedException() {
            // given
            Long storeId = 1L;
            AuthUser authUser = new AuthUser(1L, "1@naver.com", UserRole.ROLE_OWNER);

            given(userRepository.findById(authUser.getUserId())).willReturn(Optional.of(new User(authUser.getUserId(), authUser.getEmail(), authUser.getRole())));
            given(storeRepository.findById(storeId)).willReturn(Optional.empty());

            // when & then
            AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                    () -> storeOwnerService.getOneStore(authUser, storeId));

            assertEquals(ResponseCode.NOT_FOUND_STORE.getMessage(), exception.getMessage());
            verify(userRepository, times(1)).findById(authUser.getUserId());
            verify(storeRepository, times(1)).findById(storeId);
        }
    }


    @Test
    void putStore_withValidData_updatesStoreSuccessfully() {
        // given
        AuthUser authUser = new AuthUser(1L, "1@naver.com", UserRole.ROLE_OWNER);
        User user = new User(authUser.getUserId(), authUser.getEmail(), authUser.getRole());
        Long storeId = 1L;

        Store store = Store.builder()
                .image(null)
                .title("Old Store")
                .districtCategory(DistrictCategory.SEOUL_JUNG)
                .description("Old description")
                .openTime(LocalTime.of(8, 0))
                .lastOrder(LocalTime.of(21, 0))
                .isNextDay(false)
                .closeTime(LocalTime.of(22, 0))
                .turnover(LocalTime.of(1, 0))
                .reservationTableCount(3L)
                .tableCount(10L)
                .phoneNumber("111-222-3333")
                .address("Old Address")
                .deposit(1000L)
                .latitude(37.5651)
                .longitude(126.9780)
                .user(user)
                .reservations(null)
                .build();
        ReflectionTestUtils.setField(store, "id", storeId);

        StoreRequest.Create storeRequestDto = new StoreRequest.Create(
                "New Store",
                "R012",
                "Updated description",
                LocalTime.of(9, 0),
                LocalTime.of(22, 0),
                LocalTime.of(23, 0),
                LocalTime.of(0, 45),
                6L,
                15L,
                "999-888-7777", // 업데이트된 전화번호
                "New Address",
                2000L,
                37.5670,
                126.9781
        );

        MockMultipartFile image = null;

        // Mock 설정
        given(userRepository.findById(authUser.getUserId())).willReturn(Optional.of(user));
        given(storeRepository.findById(storeId)).willReturn(Optional.of(store));

        // when
        StoreResponse.Info response = storeOwnerService.putStore(authUser, storeId, storeRequestDto, image);

        // then
        assertNotNull(response);
        assertEquals(storeRequestDto.phoneNumber(), response.phoneNumber()); // 전화번호 검증
        assertEquals(storeRequestDto.title(), response.title());
        assertEquals(storeRequestDto.description(), response.description());
        assertEquals(storeRequestDto.address(), response.address());

        // Mock 호출 확인
        verify(userRepository, times(1)).findById(authUser.getUserId());
        verify(storeRepository, times(1)).findById(storeId);
        verify(s3Service, never()).deleteFile(anyString());
        verify(s3Service, never()).uploadFile(any());
    }

    @Nested
    class updateCategory {
        @Test
        void updateCategory_withValidAuthUserAndStoreId_updatesCategorySuccessfully() {
            // given
            AuthUser authUser = new AuthUser(1L, "1@naver.com", UserRole.ROLE_OWNER);
            User user = new User(authUser.getUserId(), authUser.getEmail(), authUser.getRole());
            Long storeId = 1L;

            Store store = Store.builder()
                    .image("test-image.jpg")
                    .title("Test Store")
                    .districtCategory(DistrictCategory.SEOUL_JUNG)
                    .description("Test description")
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
                    .user(user)
                    .reservations(null)
                    .build();
            ReflectionTestUtils.setField(store, "id", storeId);

            StoreRequest.Category updateCategoryRequest = new StoreRequest.Category("R013"); // 새로운 카테고리 코드

            // Mock 설정
            given(userRepository.findById(authUser.getUserId())).willReturn(Optional.of(user));
            given(storeRepository.findById(storeId)).willReturn(Optional.of(store));

            // when
            StoreResponse.Info response = storeOwnerService.updateCategory(authUser, storeId, updateCategoryRequest);

            // then
            verify(userRepository, times(1)).findById(authUser.getUserId());
            verify(storeRepository, times(1)).findById(storeId);
            assertNotNull(response);
            assertEquals(updateCategoryRequest.categoryCode(), store.getDistrictCategory().getCode()); // 카테고리 코드가 업데이트되었는지 확인
            assertEquals(updateCategoryRequest.categoryCode(), response.districtCategory().getCode()); // 응답 값 검증
        }
    }

    @Nested
    class deleteStore {
        @Test
        void deleteStore_withValidAuthUserAndStoreId_deletesStoreSuccessfully() {
            // given
            AuthUser authUser = new AuthUser(1L, "1@naver.com", UserRole.ROLE_OWNER);
            User user = new User(authUser.getUserId(), authUser.getEmail(), authUser.getRole());
            Long storeId = 1L;

            Store store = Store.builder()
                    .image("test-image.jpg")
                    .title("Test Store")
                    .districtCategory(DistrictCategory.SEOUL_JUNG)
                    .description("Test description")
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
                    .user(user)
                    .reservations(null)
                    .build();
            ReflectionTestUtils.setField(store, "id", storeId);

            // Mock 설정
            given(userRepository.findById(authUser.getUserId())).willReturn(Optional.of(user));
            given(storeRepository.findById(storeId)).willReturn(Optional.of(store));
            doNothing().when(s3Service).deleteFile(store.getImage());

            // when
            storeOwnerService.deleteStore(authUser, storeId);

            // then
            verify(userRepository, times(1)).findById(authUser.getUserId());
            verify(storeRepository, times(1)).findById(storeId);
            verify(s3Service, times(1)).deleteFile(store.getImage()); // S3 이미지 삭제 검증

            // 상태 검증: Store가 삭제 상태로 변경되었는지 확인
            assertTrue(store.getIsDeleted()); // isDeleted 메서드를 사용해서 확인
        }
    }
}