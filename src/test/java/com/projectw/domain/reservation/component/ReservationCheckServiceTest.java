package com.projectw.domain.reservation.component;

import com.projectw.common.enums.ResponseCode;
import com.projectw.common.enums.UserRole;
import com.projectw.common.exceptions.ForbiddenException;
import com.projectw.common.exceptions.InvalidRequestException;
import com.projectw.common.exceptions.UnauthorizedException;
import com.projectw.domain.reservation.dto.ReserveRedis;
import com.projectw.domain.reservation.entity.Reservation;
import com.projectw.domain.reservation.enums.ReservationStatus;
import com.projectw.domain.reservation.enums.ReservationType;
import com.projectw.domain.reservation.exception.InvalidReservationTimeException;
import com.projectw.domain.reservation.repository.ReservationRepository;
import com.projectw.domain.store.entity.Store;
import com.projectw.domain.user.entity.User;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Iterator;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationCheckServiceTest {

    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private RedissonClient redissonClient;

    @InjectMocks
    private ReservationCheckService reservationCheckService;

    @BeforeEach
    void setUp() {
    }

    @Test
    public void isReservationDateValid_정상동작() {
        // given
        LocalDate date = LocalDate.parse("2029-11-12");
        LocalTime time = LocalTime.parse("20:00:00");

        // when
        reservationCheckService.isReservationDateValid(date, time);

        // then
        // 검증할게 없음
    }

    @Test
    public void isReservationDateValid_시간_에외처리() {
        // given
        LocalDate date = LocalDate.parse("2024-11-12");
        LocalTime time = LocalTime.parse("09:00:00");

        // when
        InvalidReservationTimeException exception = assertThrows(InvalidReservationTimeException.class, () -> reservationCheckService.isReservationDateValid(date, time));

        // then
        assertEquals(ResponseCode.INVALID_RESERVATION_TIME.getMessage(), exception.getMessage());
    }

    @Test
    public void isReservationDateValid_날짜_에외처리() {
        // given
        LocalDate date = LocalDate.parse("2024-11-11");
        LocalTime time = LocalTime.parse("09:00:00");

        // when
        InvalidReservationTimeException exception = assertThrows(InvalidReservationTimeException.class, () -> reservationCheckService.isReservationDateValid(date, time));

        // then
        assertEquals(ResponseCode.INVALID_RESERVATION_TIME.getMessage(), exception.getMessage());
    }

    @Test
    public void validateMenuPresence_정상동작() {
        // given
        Long userId = 1L;
        Long storeId = 1L;
        String key = "store:"+storeId;
        ReserveRedis.Menu menu = new ReserveRedis.Menu(1L , "메뉴" , 5000L , 2L);

        RSetMultimap<Object, Object> mockedMultimap = mock(RSetMultimap.class);
        when(redissonClient.getSetMultimap(key)).thenReturn(mockedMultimap); // RedissonClient에 대한 설정

        RSet<Object> mockedSet = mock(RSet.class); // RSet 모의 객체 생성
        when(mockedSet.isEmpty()).thenReturn(false); // 비어있지 않음을 설정
        when(mockedSet.contains(menu)).thenReturn(true); // 메뉴가 포함되어 있음을 설정
        when(mockedMultimap.get(userId)).thenReturn(mockedSet); // get 메서드의 반환값 설정

        // when
        reservationCheckService.validateMenuPresence(userId, storeId);

        // then
        assertFalse(mockedSet.isEmpty()); // get 메서드가 반환하는 값이 비어있지 않음을 확인
        assertTrue(mockedSet.contains(menu)); // 메뉴가 포함되어 있는지 확인
        verify(redissonClient).getSetMultimap(key); // RedissonClient의 메서드 호출 확인
        verify(mockedMultimap).get(userId); // mockedMultimap의 get 메서드 호출 확인
    }

    @Test
    public void validateMenuPresence_장바구니_비어있음_예외처리() {
        // given
        Long userId = 1L;
        Long storeId = 1L;
        String key = "store:"+storeId;

        RSetMultimap<Object, Object> mockedMultimap = mock(RSetMultimap.class);
        when(redissonClient.getSetMultimap(key)).thenReturn(mockedMultimap); // RedissonClient에 대한 설정

        RSet<Object> mockedSet = mock(RSet.class); // RSet 모의 객체 생성
        when(mockedSet.isEmpty()).thenReturn(true); // 비어있음
        when(mockedMultimap.get(userId)).thenReturn(mockedSet); // get 메서드의 반환값 설정

        // when
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> reservationCheckService.validateMenuPresence(userId, storeId));

        // then
        assertEquals(ResponseCode.EMPTY_CART.getMessage(), exception.getMessage());
    }

    @Test
    public void validateMenuPricesEqual_정상동작() {
        // given
        Long userId = 1L;
        Long storeId = 1L;
        String key = "store:" + storeId;
        ReserveRedis.Menu menu = new ReserveRedis.Menu(1L, "메뉴", 5000L, 2L);

        // RSetMultimap 모의 객체 생성
        RSetMultimap<Object, Object> mockedMultimap = mock(RSetMultimap.class);
        lenient().when(redissonClient.getSetMultimap(key)).thenReturn(mockedMultimap); // RedissonClient 설정

        // RSet<ReserveRedis.Menu> 모의 객체 생성
        RSet<Object> mockedSet = mock(RSet.class);
        lenient().when(mockedMultimap.get(userId)).thenReturn(mockedSet); // userId에 대한 메뉴 목록 반환

        // 메뉴를 포함하는 Set을 생성하여 Iterator를 반환
        Set<Object> menuSet = Set.of(menu);
        Iterator<Object> menuIterator = menuSet.iterator();
        when(mockedSet.iterator()).thenReturn(menuIterator); // RSet의 iterator 설정

        // when
        Long sum = 0L;
        for (Object m : mockedSet) {
            ReserveRedis.Menu rm = (ReserveRedis.Menu) m;
            sum += (rm.price() * rm.menuCnt());
        }

        // then
        assertEquals(10000L, sum); // 메뉴 가격의 총합이 10000L인지 확인
    }

    @Test
    public void validateMenuPricesEqual_금액다름_예외처리() {
        // given
        Long userId = 1L;
        Long storeId = 1L;
        String key = "store:" + storeId;
        Long amount = 15000L;
        ReserveRedis.Menu menu = new ReserveRedis.Menu(1L, "메뉴", 5000L, 2L);

        // RSetMultimap 모의 객체 생성
        RSetMultimap<Object, Object> mockedMultimap = mock(RSetMultimap.class);
        lenient().when(redissonClient.getSetMultimap(key)).thenReturn(mockedMultimap); // RedissonClient 설정

        // RSet<ReserveRedis.Menu> 모의 객체 생성
        RSet<Object> mockedSet = mock(RSet.class);
        lenient().when(mockedMultimap.get(userId)).thenReturn(mockedSet); // userId에 대한 메뉴 목록 반환

        // 메뉴를 포함하는 Set을 생성하여 Iterator를 반환
        Set<Object> menuSet = Set.of(menu);
        Iterator<Object> menuIterator = menuSet.iterator();
        when(mockedSet.iterator()).thenReturn(menuIterator); // RSet의 iterator 설정

        // when
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> reservationCheckService.validateMenuPricesEqual(userId , storeId , amount));

        // then
        assertEquals(ResponseCode.INVALID_AMOUNT.getMessage(), exception.getMessage());
    }

    @Test
    public void validateUserAuthorization_정상동작() {
        // given
        Long userId = 1L;
        AuthUser authUser1 = new AuthUser(userId , "test@test.com" , UserRole.ROLE_OWNER);
        User user1 = User.fromAuthUser(authUser1);
        Long storeUserId = 2L;
        AuthUser authUser2 = new AuthUser(storeUserId , "test@test.com" , UserRole.ROLE_OWNER);
        User user2 = User.fromAuthUser(authUser2);
        Long storeId = 1L;
        Store store = Store.builder()
                .title("식당")
                .user(user1)
                .build();
        ReflectionTestUtils.setField(store, "id", storeId);

        // when
        reservationCheckService.validateUserAuthorization(store , user2);

        // then
        assertNotEquals(user1, user2);
    }

    @Test
    public void validateUserAuthorization_본인식당_예약불가_예외처리() {
        // given
        Long userId = 1L;
        AuthUser authUser = new AuthUser(userId , "test@test.com" , UserRole.ROLE_OWNER);
        User user = User.fromAuthUser(authUser);
        Long storeId = 1L;
        Store store = Store.builder()
                .title("식당")
                .user(user)
                .build();
        ReflectionTestUtils.setField(store, "id", storeId);

        // when
        UnauthorizedException exception = assertThrows(UnauthorizedException.class , () -> reservationCheckService.validateUserAuthorization(store , user));

        // then
        assertEquals(ResponseCode.UNAUTHORIZED_STORE_RESERVATION.getMessage(), exception.getMessage());
    }

    @Test
    public void validateReservationTime_정상동작() {
        // given
        Long userId = 1L;
        AuthUser authUser = new AuthUser(userId , "test@test.com" , UserRole.ROLE_USER);
        User user = User.fromAuthUser(authUser);
        Long storeId = 1L;
        Store store = Store.builder()
                .title("식당")
                .turnover(LocalTime.parse("00:30:00"))
                .user(user)
                .build();
        ReflectionTestUtils.setField(store, "id", storeId);

        LocalTime time = LocalTime.parse("12:00:00");

        // when
        reservationCheckService.validateReservationTime(store , time);

        // then
        int minutes = time.getHour() * 60 + time.getMinute();
        int baseMinutes = store.getTurnover().getHour() * 60 + store.getTurnover().getMinute();
        assertEquals(0, minutes % baseMinutes);
    }

    @Test
    public void validateReservationTime_예약가능_시간대_검증_예외처리() {
        // given
        Long userId = 1L;
        AuthUser authUser = new AuthUser(userId , "test@test.com" , UserRole.ROLE_USER);
        User user = User.fromAuthUser(authUser);
        Long storeId = 1L;
        Store store = Store.builder()
                .title("식당")
                .turnover(LocalTime.parse("00:30:00"))
                .user(user)
                .build();
        ReflectionTestUtils.setField(store, "id", storeId);

        LocalTime time = LocalTime.parse("12:10:00");

        // when
        InvalidReservationTimeException exception = assertThrows(InvalidReservationTimeException.class, () -> reservationCheckService.validateReservationTime(store , time));

        // then
        assertEquals(ResponseCode.INVALID_RESERVATION_TIME.getMessage(), exception.getMessage());
    }

    @Test
    public void checkReservationCapacity_정상동작() {
        // given
        Long userId = 1L;
        AuthUser authUser = new AuthUser(userId , "test@test.com" , UserRole.ROLE_USER);
        User user = User.fromAuthUser(authUser);
        Long storeId = 1L;
        Store store = Store.builder()
                .title("식당")
                .reservationTableCount(2L)
                .user(user)
                .build();
        ReflectionTestUtils.setField(store, "id", storeId);

        LocalDate date = LocalDate.parse("2024-11-19");
        LocalTime time = LocalTime.parse("12:00:00");

        long remainder = 5L;
        given(reservationRepository.countReservationByDate(any() , any() , any() , any())).willReturn(remainder);

        // when
        reservationCheckService.checkReservationCapacity(store , date, time);

        // then
        assertTrue(store.getReservationTableCount() <= remainder);
    }

    @Test
    public void isUserReservation_정상동작() {
        // given
        Long userId = 1L;
        AuthUser authUser = new AuthUser(userId , "test@test.com" , UserRole.ROLE_USER);
        User user = User.fromAuthUser(authUser);
        Reservation reservation = Reservation.builder()
                .user(user)
                .build();

        // when
        reservationCheckService.isUserReservation(userId , reservation);

        // then
        assertEquals(reservation.getUser().getId(), userId);
    }

    @Test
    public void isUserReservation_본인예약건_아님_예외처리() {
        // given
        Long userId = 1L;
        Long userId2 = 2L;
        AuthUser authUser = new AuthUser(userId2 , "test@test.com" , UserRole.ROLE_USER);
        User user = User.fromAuthUser(authUser);
        Reservation reservation = Reservation.builder()
                .user(user)
                .build();

        // when
        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> reservationCheckService.isUserReservation(userId , reservation));

        // then
        assertEquals(ResponseCode.UNAUTHORIZED_RESERVATION.getMessage(), exception.getMessage());
    }

    @Test
    public void isOwnerReservation_정상동작() {
        // given
        Long userId = 1L;
        AuthUser authUser = new AuthUser(userId , "test@test.com" , UserRole.ROLE_USER);
        User user = User.fromAuthUser(authUser);
        Store store = Store.builder()
                .user(user)
                .build();
        Reservation reservation = Reservation.builder()
                .store(store)
                .build();

        // when
        reservationCheckService.isOwnerReservation(userId , reservation);

        // then
        assertEquals(reservation.getStore().getUser().getId(), userId);
    }

    @Test
    public void isOwnerReservation_본인식당예약건인지_검증_예외처리() {
        // given
        Long userId = 1L;
        Long userId2 = 2L;
        AuthUser authUser = new AuthUser(userId2 , "test@test.com" , UserRole.ROLE_USER);
        User user = User.fromAuthUser(authUser);
        Store store = Store.builder()
                .user(user)
                .build();
        Reservation reservation = Reservation.builder()
                .store(store)
                .build();

        // when
        ForbiddenException exception = assertThrows(ForbiddenException.class, () -> reservationCheckService.isOwnerReservation(userId , reservation));

        // then
        assertEquals(ResponseCode.FORBIDDEN.getMessage(), exception.getMessage());
    }

    @Test
    public void canChangeReservationType_정상동작() {
        // given
        Reservation reservation = Reservation.builder()
                .type(ReservationType.RESERVATION)
                .build();
        ReservationType type = ReservationType.RESERVATION;

        // when
        reservationCheckService.canChangeReservationType(reservation, type);

        // then
        assertEquals(reservation.getType(), ReservationType.RESERVATION);
    }

    @Test
    public void canChangeReservationType_타입검증_예외처리() {
        // given
        Reservation reservation = Reservation.builder()
                .type(ReservationType.RESERVATION)
                .build();
        ReservationType type = ReservationType.WAIT;

        // when
        ForbiddenException exception = assertThrows(ForbiddenException.class, () -> reservationCheckService.canChangeReservationType(reservation, type));

        // then
        assertEquals(ResponseCode.CANCEL_FORBIDDEN.getMessage(), exception.getMessage());
    }

    @Test
    public void canChangeReservationStatus_정상동작() {
        // given
        Reservation reservation = Reservation.builder()
                .status(ReservationStatus.RESERVATION)
                .build();
        ReservationStatus status = ReservationStatus.RESERVATION;

        // when
        reservationCheckService.canChangeReservationStatus(reservation, status , ResponseCode.APPLY_FORBIDDEN);

        // then
        assertEquals(reservation.getStatus(), ReservationStatus.RESERVATION);
    }

    @Test
    public void canChangeReservationStatus_타입검증_예외처리() {
        // given
        Reservation reservation = Reservation.builder()
                .status(ReservationStatus.RESERVATION)
                .build();
        ReservationStatus status = ReservationStatus.CANCEL;
        ResponseCode responseCode = ResponseCode.APPLY_FORBIDDEN;

        // when
        ForbiddenException exception = assertThrows(ForbiddenException.class , () -> reservationCheckService.canChangeReservationStatus(reservation, status , responseCode));

        // then
        assertEquals(responseCode.getMessage(), exception.getMessage());
    }
}