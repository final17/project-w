package com.projectw.domain.reservation.service;

import com.projectw.common.enums.ResponseCode;
import com.projectw.common.enums.UserRole;
import com.projectw.common.exceptions.ForbiddenException;
import com.projectw.common.exceptions.NotFoundException;
import com.projectw.common.utils.Scheduler;
import com.projectw.domain.menu.entity.Menu;
import com.projectw.domain.menu.repository.MenuRepository;
import com.projectw.domain.payment.event.PaymentCancelEvent;
import com.projectw.domain.payment.event.PaymentTimeoutCancelEvent;
import com.projectw.domain.reservation.component.ReservationCheckService;
import com.projectw.domain.reservation.dto.ReserveMenuRequest;
import com.projectw.domain.reservation.dto.ReserveRedis;
import com.projectw.domain.reservation.dto.ReserveRequest;
import com.projectw.domain.reservation.dto.ReserveResponse;
import com.projectw.domain.reservation.entity.Reservation;
import com.projectw.domain.reservation.entity.ReservationMenu;
import com.projectw.domain.reservation.enums.ReservationStatus;
import com.projectw.domain.reservation.enums.ReservationType;
import com.projectw.domain.reservation.exception.InvalidCartException;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

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

        ReserveRedis.Menu rm = new ReserveRedis.Menu(menu.getId() , menu.getName() , (long) menu.getPrice(), 1L);

        RSet<Object> mockedSet = mock(RSet.class); // RSet 모의 객체 생성
        when(mockedSet.toArray()).thenReturn(new Object[]{rm}); // 배열 반환
        when(mockedMultimap.get(insertReservation.user().getId())).thenReturn(mockedSet); // get 메서드의 반환값 설정

        List<Menu> menuList = List.of(menu);
        given(menuRepository.getMenus(any())).willReturn(menuList);

        List<ReservationMenu> reservationMenus = List.of(
                new ReservationMenu(menu , menu.getName() , (long) menu.getPrice(), 1L , reservation)
        );

        given(reservationMenuRepository.saveAll(any())).willReturn(reservationMenus);

        doNothing().when(scheduler).scheduleOnceAfterDelay(anyLong() , any() , any() , any());

        // when
        reservationService.prepareReservation(insertReservation);

        // then
        assertEquals(orderId , reservation.getOrderId());
        verify(scheduler).scheduleOnceAfterDelay(anyLong() , any() , any() , any());
    }

    @Test
    public void prepareReservation_장바구니메뉴와_DB메뉴_불일치_예외처리() throws Exception {
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

        ReserveRedis.Menu rm = new ReserveRedis.Menu(2L , menu.getName() , (long) menu.getPrice(), 1L);

        RSet<Object> mockedSet = mock(RSet.class); // RSet 모의 객체 생성
        when(mockedSet.toArray()).thenReturn(new Object[]{rm}); // 배열 반환
        when(mockedMultimap.get(insertReservation.user().getId())).thenReturn(mockedSet); // get 메서드의 반환값 설정

        List<Menu> menuList = List.of(menu);
        given(menuRepository.getMenus(any())).willReturn(menuList);

        // when
        NotFoundException exception = assertThrows(NotFoundException.class , () -> reservationService.prepareReservation(insertReservation));

        // then
        assertEquals(ResponseCode.NOT_FOUND_MENU.getMessage() , exception.getMessage());
    }

    @Test
    public void prepareReservation_메뉴길이다름_예외처리() throws Exception {
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

        ReserveRedis.Menu rm = new ReserveRedis.Menu(menu.getId() , menu.getName() , (long) menu.getPrice(), 1L);

        RSet<Object> mockedSet = mock(RSet.class); // RSet 모의 객체 생성
        when(mockedSet.toArray()).thenReturn(new Object[]{rm}); // 배열 반환
        when(mockedMultimap.get(insertReservation.user().getId())).thenReturn(mockedSet); // get 메서드의 반환값 설정

        List<Menu> menuList = List.of(menu , menu);
        given(menuRepository.getMenus(any())).willReturn(menuList);

        // when
        NotFoundException exception = assertThrows(NotFoundException.class , () -> reservationService.prepareReservation(insertReservation));

        // then
        assertEquals(ResponseCode.NOT_FOUND_MENU.getMessage() , exception.getMessage());
    }

    @Test
    public void successReservation_동작완료() {
        // given
        String orderId = PREFIX_ORDER_ID + "ASDAS232ASDAS";
        Reservation reservation = Reservation.builder()
                .orderId(orderId)
                .paymentYN(false)
                .build();
        given(reservationRepository.findByOrderId(anyString())).willReturn(Optional.of(reservation));

        // when
        reservationService.successReservation(orderId);

        // then
        assertEquals(orderId , reservation.getOrderId());
        assertTrue(reservation.isPaymentYN());
    }

    @Test
    public void successReservation_결제정보없음_예외처리() {
        // given
        String orderId = PREFIX_ORDER_ID + "ASDAS232ASDAS";
        given(reservationRepository.findByOrderId(anyString())).willReturn(Optional.empty());

        // when
        NotFoundException exception = assertThrows(NotFoundException.class , () -> reservationService.successReservation(orderId));

        // then
        assertEquals(ResponseCode.NOT_FOUND_RESERVATION.getMessage() , exception.getMessage());
    }

    @Test
    public void cancelReservation_동작완료() {
        // given
        Long userId = 1L;
        Long storeId = 1L;
        Long reservationId = 1L;
        ReserveRequest.Cancel cancel = new ReserveRequest.Cancel("단순 변심");
        Reservation reservation = Reservation.builder()
                .status(ReservationStatus.APPLY)
                .paymentYN(true)
                .build();

        given(reservationRepository.findByIdAndStoreId(anyLong(), anyLong())).willReturn(Optional.of(reservation));
        doNothing().when(reservationCheckService).isUserReservation(userId , reservation);
        doNothing().when(reservationCheckService).canChangeReservationType(reservation , ReservationType.RESERVATION);
        doNothing().when(eventPublisher).publishEvent(any(PaymentCancelEvent.class));

        // when
        reservationService.cancelReservation(userId , storeId , reservationId , cancel);

        // then
        assertEquals(ReservationStatus.CANCEL , reservation.getStatus());
    }

    @Test
    public void cancelReservation_변경불가능한_상태값_예외처리() {
        // given
        Long userId = 1L;
        Long storeId = 1L;
        Long reservationId = 1L;
        ReserveRequest.Cancel cancel = new ReserveRequest.Cancel("단순 변심");
        Reservation reservation = Reservation.builder()
                .status(ReservationStatus.COMPLETE)
                .paymentYN(true)
                .build();

        given(reservationRepository.findByIdAndStoreId(anyLong(), anyLong())).willReturn(Optional.of(reservation));
        doNothing().when(reservationCheckService).isUserReservation(userId , reservation);
        doNothing().when(reservationCheckService).canChangeReservationType(reservation , ReservationType.RESERVATION);

        // when
        ForbiddenException exception = assertThrows(ForbiddenException.class , () -> reservationService.cancelReservation(userId , storeId , reservationId , cancel));

        // then
        assertEquals(ResponseCode.CANCEL_FORBIDDEN.getMessage() , exception.getMessage());
    }

    @Test
    public void cancelReservation_결제정보없음_예외처리() {
        // given
        Long userId = 1L;
        Long storeId = 1L;
        Long reservationId = 1L;
        ReserveRequest.Cancel cancel = new ReserveRequest.Cancel("단순 변심");

        given(reservationRepository.findByIdAndStoreId(anyLong(), anyLong())).willReturn(Optional.empty());

        // when
        NotFoundException exception = assertThrows(NotFoundException.class , () -> reservationService.cancelReservation(userId , storeId , reservationId , cancel));

        // then
        assertEquals(ResponseCode.NOT_FOUND_RESERVATION.getMessage() , exception.getMessage());
    }

    @Test
    public void autoCancelMethod_결제전_정상동작() {
        // given
        Long reservationId = 1L;
        Reservation reservation = Reservation.builder()
                .status(ReservationStatus.RESERVATION)
                .paymentYN(false)
                .build();

        given(reservationRepository.findById(anyLong())).willReturn(Optional.of(reservation));
        given(reservationRepository.save(any(Reservation.class))).willReturn(reservation);
        doNothing().when(eventPublisher).publishEvent(any(PaymentTimeoutCancelEvent.class));

        // when
        reservationService.autoCancelMethod(reservationId);

        // then
        assertEquals(ReservationStatus.CANCEL , reservation.getStatus());
    }

    @Test
    public void autoCancelMethod_결제완료_정상동작() {
        // given
        Long reservationId = 1L;
        Reservation reservation = Reservation.builder()
                .status(ReservationStatus.COMPLETE)
                .paymentYN(true)
                .build();
        given(reservationRepository.findById(anyLong())).willReturn(Optional.of(reservation));

        // when
        reservationService.autoCancelMethod(reservationId);

        // then
        assertEquals(ReservationStatus.COMPLETE , reservation.getStatus());
    }

    @Test
    public void autoCancelMethod_결제정보없음_예외처리() {
        // given
        Long reservationId = 1L;
        Reservation reservation = Reservation.builder()
                .status(ReservationStatus.COMPLETE)
                .paymentYN(true)
                .build();
        given(reservationRepository.findById(anyLong())).willReturn(Optional.empty());

        // when
        NotFoundException exception = assertThrows(NotFoundException.class , () -> reservationService.autoCancelMethod(reservationId));

        // then
        assertEquals(ResponseCode.NOT_FOUND_RESERVATION.getMessage() , exception.getMessage());
    }

    @Test
    public void getUserReservations_정상동작() {
        // given
        Long userId = 1L;
        String orderId = PREFIX_ORDER_ID + "ASDAS232ASDAS";
        ReserveRequest.Parameter parameter = new ReserveRequest.Parameter(ReservationType.RESERVATION , ReservationStatus.RESERVATION , LocalDate.parse("2024-11-10") , LocalDate.parse("2024-11-20") , 1 , 10);

        List<ReserveResponse.Infos> infos = List.of(
                new ReserveResponse.Infos(orderId , 1L , 1L , 1L , 1L , 1L , 5000L , true , LocalDate.parse("2024-11-12") , LocalTime.parse("11:00:00") , ReservationType.RESERVATION , ReservationStatus.RESERVATION)
        );
        Pageable pageable = PageRequest.of(parameter.page() - 1, parameter.size());
        Page<ReserveResponse.Infos> page = new PageImpl<>(infos , pageable , 1L);

        given(reservationRepository.getUserReservations(anyLong(), any() , any())).willReturn(page);

        // when
        page = reservationService.getUserReservations(userId , parameter);

        // then
        assertEquals(orderId , page.getContent().get(0).orderId());
    }

    @Test
    public void addCartItem_정상동작() throws Exception {
        // given
        Long userId = 1L;
        Long storeId = 1L;
        ReserveMenuRequest.Menu menuRequest = new ReserveMenuRequest.Menu(menu.getId() , 1L);
        List<ReserveMenuRequest.Menu> menusRequest = List.of(menuRequest);
        ReserveRequest.AddCart addCart = new ReserveRequest.AddCart(menusRequest);

        Method method = ReservationService.class.getDeclaredMethod("assembleCartRedisKey", Long.class);
        method.setAccessible(true);  // private 메서드에 접근 가능하도록 설정
        String key = (String) method.invoke(reservationService, storeId);

        RSetMultimap<Object, Object> mockedMultimap = mock(RSetMultimap.class);
        when(redissonClient.getSetMultimap(key)).thenReturn(mockedMultimap); // RedissonClient 설정

        given(menuRepository.findByIdAndStoreId(anyLong(), anyLong())).willReturn(Optional.of(menu));

        ReserveRedis.Menu rm = new ReserveRedis.Menu(menu.getId() , menu.getName() , (long) menu.getPrice(), 1L);
        RSet<Object> mockedSet = mock(RSet.class); // RSet 모의 객체 생성

        Set<Object> s = Set.of(rm);
        when(mockedSet.iterator()).thenReturn(s.iterator());
        when(mockedMultimap.get(userId)).thenReturn(mockedSet); // get 메서드의 반환값 설정

        // when
        reservationService.addCartItem(userId , storeId , addCart);

        // then
        assertEquals(rm.menuId() , menu.getId());
    }

    @Test
    public void addCartItem_메뉴없음_예외처리() throws Exception {
        // given
        Long userId = 1L;
        Long storeId = 1L;
        ReserveMenuRequest.Menu menuRequest = new ReserveMenuRequest.Menu(menu.getId() , 1L);
        List<ReserveMenuRequest.Menu> menusRequest = List.of(menuRequest);
        ReserveRequest.AddCart addCart = new ReserveRequest.AddCart(menusRequest);

        Method method = ReservationService.class.getDeclaredMethod("assembleCartRedisKey", Long.class);
        method.setAccessible(true);  // private 메서드에 접근 가능하도록 설정
        String key = (String) method.invoke(reservationService, storeId);

        RSetMultimap<Object, Object> mockedMultimap = mock(RSetMultimap.class);
        when(redissonClient.getSetMultimap(key)).thenReturn(mockedMultimap); // RedissonClient 설정

        given(menuRepository.findByIdAndStoreId(anyLong(), anyLong())).willReturn(Optional.empty());

        // when
        NotFoundException exception = assertThrows(NotFoundException.class , () -> reservationService.addCartItem(userId , storeId , addCart));

        // then
        assertEquals(ResponseCode.NOT_FOUND_MENU.getMessage() , exception.getMessage());
    }

    @Test
    public void updateCartItem_정상동작() throws Exception {
        // given
        Long userId = 1L;
        Long storeId = 1L;
        Long menuCnt = 2L;
        ReserveRequest.UpdateCart updateCart = new ReserveRequest.UpdateCart(menu.getId() , menuCnt);

        Method method = ReservationService.class.getDeclaredMethod("assembleCartRedisKey", Long.class);
        method.setAccessible(true);  // private 메서드에 접근 가능하도록 설정
        String key = (String) method.invoke(reservationService, storeId);

        RSetMultimap<Object, Object> mockedMultimap = mock(RSetMultimap.class);
        when(redissonClient.getSetMultimap(key)).thenReturn(mockedMultimap); // RedissonClient 설정

        ReserveRedis.Menu rm = new ReserveRedis.Menu(menu.getId() , menu.getName() , (long) menu.getPrice(), 1L);
        RSet<Object> mockedSet = mock(RSet.class); // RSet 모의 객체 생성
        Set<Object> s = Set.of(rm);
        when(mockedSet.iterator()).thenReturn(s.iterator());
        when(mockedMultimap.get(userId)).thenReturn(mockedSet); // get 메서드의 반환값 설정
        when(mockedMultimap.remove(anyLong() , any())).thenReturn(true);
        when(mockedMultimap.put(anyLong() , any())).thenReturn(true);

        // when
        reservationService.updateCartItem(userId , storeId , updateCart);

        // then
        verify(mockedMultimap).remove(anyLong() , any());
        verify(mockedMultimap).put(anyLong() , any());
    }

    @Test
    public void updateCartItem_수정메뉴없음_예외처리() throws Exception {
        // given
        Long userId = 1L;
        Long storeId = 1L;
        Long menuCnt = 2L;
        ReserveRequest.UpdateCart updateCart = new ReserveRequest.UpdateCart(2L , menuCnt);

        Method method = ReservationService.class.getDeclaredMethod("assembleCartRedisKey", Long.class);
        method.setAccessible(true);  // private 메서드에 접근 가능하도록 설정
        String key = (String) method.invoke(reservationService, storeId);

        RSetMultimap<Object, Object> mockedMultimap = mock(RSetMultimap.class);
        when(redissonClient.getSetMultimap(key)).thenReturn(mockedMultimap); // RedissonClient 설정

        ReserveRedis.Menu rm = new ReserveRedis.Menu(menu.getId() , menu.getName() , (long) menu.getPrice(), 1L);
        RSet<Object> mockedSet = mock(RSet.class); // RSet 모의 객체 생성
        Set<Object> s = Set.of(rm);
        when(mockedSet.iterator()).thenReturn(s.iterator());
        when(mockedMultimap.get(userId)).thenReturn(mockedSet); // get 메서드의 반환값 설정

        // when
        InvalidCartException exception = assertThrows(InvalidCartException.class , () -> reservationService.updateCartItem(userId , storeId , updateCart));

        // then
        assertEquals(ResponseCode.INVALID_CART.getMessage() , exception.getMessage());
    }

    @Test
    public void removeCartItem_정상동작() throws Exception {
        // given
        Long userId = 1L;
        Long storeId = 1L;
        ReserveRequest.RemoveCart removeCart = new ReserveRequest.RemoveCart(menu.getId());

        Method method = ReservationService.class.getDeclaredMethod("assembleCartRedisKey", Long.class);
        method.setAccessible(true);  // private 메서드에 접근 가능하도록 설정
        String key = (String) method.invoke(reservationService, storeId);

        RSetMultimap<Object, Object> mockedMultimap = mock(RSetMultimap.class);
        when(redissonClient.getSetMultimap(key)).thenReturn(mockedMultimap); // RedissonClient 설정

        ReserveRedis.Menu rm = new ReserveRedis.Menu(menu.getId() , menu.getName() , (long) menu.getPrice(), 1L);
        RSet<Object> mockedSet = mock(RSet.class); // RSet 모의 객체 생성
        Set<Object> s = Set.of(rm);
        when(mockedSet.iterator()).thenReturn(s.iterator());
        when(mockedMultimap.get(anyLong())).thenReturn(mockedSet); // get 메서드의 반환값 설정
        when(mockedMultimap.remove(anyLong() , any())).thenReturn(true);

        // when
        reservationService.removeCartItem(userId , storeId , removeCart);

        // then
        verify(mockedMultimap).remove(anyLong() , any());
    }

    @Test
    public void getCartItems_정상동작() throws Exception {
        // given
        Long userId = 1L;
        Long storeId = 1L;

        Method method = ReservationService.class.getDeclaredMethod("assembleCartRedisKey", Long.class);
        method.setAccessible(true);  // private 메서드에 접근 가능하도록 설정
        String key = (String) method.invoke(reservationService, storeId);

        RSetMultimap<Object, Object> mockedMultimap = mock(RSetMultimap.class);
        when(redissonClient.getSetMultimap(key)).thenReturn(mockedMultimap);

        ReserveRedis.Menu rm = new ReserveRedis.Menu(menu.getId() , menu.getName() , (long) menu.getPrice(), 1L);
        RSet<Object> mockedSet = mock(RSet.class); // RSet 모의 객체 생성
        Set<Object> s = Set.of(rm);
        when(mockedSet.iterator()).thenReturn(s.iterator());
        when(mockedMultimap.get(anyLong())).thenReturn(mockedSet); // get 메서드의 반환값 설정

        // when
        List<ReserveResponse.Carts> carts = reservationService.getCartItems(userId , storeId);

        // then
        assertEquals(1, carts.size());
        assertEquals(menu.getId() , carts.get(0).menuId());
    }
}
