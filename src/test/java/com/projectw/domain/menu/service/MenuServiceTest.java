package com.projectw.domain.menu.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
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
import com.projectw.domain.user.entity.User;
import com.projectw.security.AuthUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.redisson.api.RedissonClient;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.projectw.domain.category.DistrictCategory.SEOUL;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class MenuServiceTest {

    private MenuService menuService;

    @Mock
    private MenuRepository menuRepository;

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private AllergyRepository allergyRepository;

    @Mock
    private RedissonClient redissonClient;

    @Mock
    private ElasticsearchClient elasticsearchClient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        menuService = new MenuService(menuRepository, storeRepository, allergyRepository, redissonClient, elasticsearchClient);
    }

    @Test
    void createMenu_Success() throws Exception {
        // Given
        Long storeId = 1L;
        AuthUser authUser = new AuthUser(1L, "owner", UserRole.ROLE_OWNER);
        Store store = new Store(
                1L,
                "store-image.jpg", // image
                SEOUL, // districtCategory
                "A Store", // title
                "A cozy place", // description
                LocalTime.of(9, 0), // openTime
                LocalTime.of(22, 0), // closeTime
                false, // isNextDay
                5L, // reservationTableCount
                10L, // tableCount
                "123-456-7890", // phoneNumber
                "123 Test Street", // address
                LocalTime.of(21, 30), // lastOrder
                LocalTime.of(1, 0), // turnover
                new User(1L, "owner@email.com", UserRole.ROLE_OWNER),
                new ArrayList<>(), // reservations
                5000L, // deposit
                37.7749, // latitude
                -122.4194 // longitude
        );

        MenuRequestDto.Create requestDto = new MenuRequestDto.Create("New Menu", 5000, List.of(1L, 2L));

        Allergy allergy1 = new Allergy("Peanut", "Peanut allergy");
        Allergy allergy2 = new Allergy("Dairy", "Dairy allergy");

        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
        when(allergyRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(allergy1, allergy2));

        when(menuRepository.save(any(Menu.class))).thenAnswer(invocation -> {
            Menu menu = invocation.getArgument(0);
            return new Menu(
                    1L, // ID 설정
                    menu.getName(),
                    menu.getPrice(),
                    menu.getStore(),
                    menu.getAllergies()
            );
        });

        MenuResponseDto.Detail expectedResponse = new MenuResponseDto.Detail(
                1L,
                "New Menu",
                5000,
                Set.of("Peanut", "Dairy"), // 알레르기 목록
                0
        );

        // When
        MenuResponseDto.Detail response = menuService.createMenu(authUser, requestDto, storeId);

        // Then
        assertNotNull(response);
        assertEquals(expectedResponse.id(), response.id()); // Record 메서드 사용
        assertEquals(expectedResponse.name(), response.name());
        assertEquals(expectedResponse.price(), response.price());
        assertEquals(expectedResponse.allergies(), response.allergies());
        assertEquals(expectedResponse.viewCount(), response.viewCount());
    }

    @Test
    void createMenu_Fail_NotOwner() {
        // Given
        Long storeId = 1L;
        AuthUser authUser = new AuthUser(1L, "user", UserRole.ROLE_USER);
        Store store = new Store(
                "store-image.jpg", // image
                SEOUL, // districtCategory
                "A Store", // title
                "A cozy place", // description
                LocalTime.of(9, 0), // openTime
                LocalTime.of(22, 0), // closeTime
                false, // isNextDay
                5L, // reservationTableCount
                10L, // tableCount
                "123-456-7890", // phoneNumber
                "123 Test Street", // address
                LocalTime.of(21, 30), // lastOrder
                LocalTime.of(1, 0), // turnover
                new User(1L, "user@email.com", UserRole.ROLE_USER),
                new ArrayList<>(), // reservations
                5000L, // deposit
                37.7749, // latitude
                -122.4194 // longitude
        );

        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));

        MenuRequestDto.Create requestDto = new MenuRequestDto.Create("New Menu", 5000, List.of());

        // When & Then
        AccessDeniedException exception = assertThrows(
                AccessDeniedException.class,
                () -> menuService.createMenu(authUser, requestDto, storeId)
        );

        assertNotNull(exception);
        assertEquals(ResponseCode.FORBIDDEN, ResponseCode.FORBIDDEN);
        verify(storeRepository).findById(storeId);
        verifyNoInteractions(menuRepository);
    }

    @Test
    void updateMenu_Success() throws Exception {
        // Given
        Long storeId = 1L;
        Long menuId = 1L;
        AuthUser authUser = new AuthUser(1L, "owner", UserRole.ROLE_OWNER);
        Store store = new Store(
                1L,
                "store-image.jpg", // image
                SEOUL, // districtCategory
                "A Store", // title
                "A cozy place", // description
                LocalTime.of(9, 0), // openTime
                LocalTime.of(22, 0), // closeTime
                false, // isNextDay
                5L, // reservationTableCount
                10L, // tableCount
                "123-456-7890", // phoneNumber
                "123 Test Street", // address
                LocalTime.of(21, 30), // lastOrder
                LocalTime.of(1, 0), // turnover
                new User(1L, "owner@email.com", UserRole.ROLE_OWNER),
                new ArrayList<>(), // reservations
                5000L, // deposit
                37.7749, // latitude
                -122.4194 // longitude
        );
        Menu menu = new Menu("Old Menu", 4000, store, Set.of());
        MenuRequestDto.Update requestDto = new MenuRequestDto.Update(1L, "Pizza", 10000, List.of(1L, 2L));

        Allergy allergy = new Allergy("Peanut", "Peanut Allergy");

        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
        when(menuRepository.findById(menuId)).thenReturn(Optional.of(menu));
        when(allergyRepository.findAllById(requestDto.allergyIds())).thenReturn(List.of(allergy));
        when(menuRepository.save(any(Menu.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        MenuResponseDto.Detail response = menuService.updateMenu(authUser, requestDto, storeId, menuId);

        // Then
        assertNotNull(response);
        assertEquals(requestDto.name(), response.name());
        assertEquals(requestDto.price(), response.price());
        assertTrue(response.allergies().contains("Peanut"));

        verify(menuRepository).findById(menuId);
        verify(menuRepository).save(any(Menu.class));
    }

    @Test
    void deleteMenu_Success() {
        // Given
        Long storeId = 1L;
        Long menuId = 1L;
        AuthUser authUser = new AuthUser(1L, "owner@email.com", UserRole.ROLE_OWNER);
        Store store = new Store(
                1L,
                "store-image.jpg",
                SEOUL,
                "A Store",
                "A cozy place",
                LocalTime.of(9, 0),
                LocalTime.of(22, 0),
                false,
                5L,
                10L,
                "123-456-7890",
                "123 Test Street",
                LocalTime.of(21, 30),
                LocalTime.of(1, 0),
                new User(1L, "owner@email.com", UserRole.ROLE_OWNER),
                new ArrayList<>(),
                5000L,
                37.7749,
                -122.4194
        );

        Menu menu = new Menu(
                "Menu",
                4000,
                store,
                Set.of()
        );

        // Mock 설정
        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
        when(menuRepository.findById(menuId)).thenReturn(Optional.of(menu));

        // When
        assertDoesNotThrow(() -> menuService.deleteMenu(authUser, storeId, menuId));

        // Then
        verify(menuRepository).save(menu); // Menu 삭제 플래그 설정 후 저장 확인
    }
}