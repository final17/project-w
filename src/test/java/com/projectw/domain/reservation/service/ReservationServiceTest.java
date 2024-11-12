package com.projectw.domain.reservation.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.projectw.common.enums.UserRole;
import com.projectw.common.utils.Scheduler;
import com.projectw.domain.allergy.entity.Allergy;
import com.projectw.domain.menu.entity.Menu;
import com.projectw.domain.menu.repository.MenuRepository;
import com.projectw.domain.payment.service.PaymentService;
import com.projectw.domain.reservation.component.ReservationCheckService;
import com.projectw.domain.reservation.dto.ReserveRedis;
import com.projectw.domain.reservation.dto.ReserveRequest;
import com.projectw.domain.reservation.entity.Reservation;
import com.projectw.domain.reservation.enums.ReservationStatus;
import com.projectw.domain.reservation.enums.ReservationType;
import com.projectw.domain.reservation.repository.ReservationMenuRepository;
import com.projectw.domain.reservation.repository.ReservationRepository;
import com.projectw.domain.store.entity.Store;
import com.projectw.domain.store.repository.StoreRepository;
import com.projectw.domain.user.entity.User;
import com.projectw.domain.user.repository.UserRepository;
import com.projectw.security.AuthUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RSet;
import org.redisson.api.RSetMultimap;
import org.redisson.api.RedissonClient;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ReservationServiceTest {
    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private ReservationMenuRepository reservationMenuRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private StoreRepository storeRepository;
    @Mock
    private MenuRepository menuRepository;
    @Mock
    private ReservationCheckService reservationCheckService;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private Scheduler scheduler;
    @Mock
    private RedissonClient redissonClient;

    @InjectMocks
    private ReservationService reservationService;

    private final String PREFIX_ORDER_ID = "ORDER-";

    private AuthUser authUser;
    private User user;
    private Store store;
    private Menu menu;

    @BeforeEach
    public void setUp() {
        Long userId = 1L;
        Long storeId = 1L;
        Long menuId = 1L;
        authUser = new AuthUser(userId , "test@test.com" , UserRole.ROLE_USER);
        user = User.fromAuthUser(authUser);
        store = Store.builder()
                .title("식당")
                .user(user)
                .build();
        ReflectionTestUtils.setField(store, "id", storeId);
        menu = new Menu("메뉴" , 5000 , store , Set.of());
        ReflectionTestUtils.setField(menu, "id", menuId);
    }

    @Test
    public void prepareReservation_정상동작() throws Exception {
        // given
        String orderId = PREFIX_ORDER_ID + "ASDAS232ASDAS";
        LocalDate date = LocalDate.parse("2024-11-20");
        LocalTime time = LocalTime.parse("11:00:00");
        Long people = 2L;
        Long amount = 5000L;
        ReserveRequest.InsertReservation insertReservation = new ReserveRequest.InsertReservation(orderId , date , time , people , amount , user , store);

        Long reservationNo = 1L;
        given(reservationRepository.findMaxReservationDate(any() , any())).willReturn(reservationNo);

        Reservation reservation = Reservation.builder()
                .orderId(orderId)
                .status(ReservationStatus.RESERVATION)
                .type(ReservationType.RESERVATION)
                .reservationDate(insertReservation.reservationDate())
                .reservationTime(insertReservation.reservationTime())
                .numberPeople(insertReservation.numberPeople())
                .reservationNo(reservationNo)
                .paymentYN(false)
                .paymentAmt(insertReservation.paymentAmt())
                .user(insertReservation.user())
                .store(insertReservation.store())
                .build();

        given(reservationRepository.save(any(Reservation.class))).willReturn(reservation);

        Method method = ReservationService.class.getDeclaredMethod("assembleCartRedisKey", Long.class);
        method.setAccessible(true);  // private 메서드에 접근 가능하도록 설정
        String key = (String) method.invoke(reservationService, insertReservation.store().getId());

        RSetMultimap<Object , Object> mockedMultimap = mock(RSetMultimap.class);
        when(redissonClient.getSetMultimap(key)).thenReturn(mockedMultimap); // RedissonClient에 대한 설정

        RSet<Object> mockedSet = mock(RSet.class); // RSet 모의 객체 생성
        when(mockedSet.isEmpty()).thenReturn(false); // 비어있지 않음을 설정
        when(mockedSet.contains(menu)).thenReturn(true); // 메뉴가 포함되어 있음을 설정
        when(mockedMultimap.get(insertReservation.user().getId())).thenReturn(mockedSet); // get 메서드의 반환값 설정

        List<ReserveRedis.Menu> list = mock(ArrayList.class);


        List<Menu> menuList = List.of(menu);
        given(menuRepository.getMenus(any())).willReturn(menuList);

        // when
        reservationService.prepareReservation(insertReservation);

        // then



    }


}
