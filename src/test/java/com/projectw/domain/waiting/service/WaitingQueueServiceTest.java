package com.projectw.domain.waiting.service;

import com.projectw.common.enums.ResponseCode;
import com.projectw.common.enums.UserRole;
import com.projectw.common.exceptions.ForbiddenException;
import com.projectw.common.exceptions.NotFoundException;
import com.projectw.common.exceptions.UserAlreadyInQueueException;
import com.projectw.common.utils.RedisProducer;
import com.projectw.domain.notification.service.NotificationService;
import com.projectw.domain.reservation.entity.Reservation;
import com.projectw.domain.reservation.repository.ReservationRepository;
import com.projectw.domain.store.entity.Store;
import com.projectw.domain.store.repository.StoreRepository;
import com.projectw.domain.user.entity.User;
import com.projectw.domain.user.repository.UserRepository;
import com.projectw.domain.waiting.dto.WaitingQueueResponse;
import com.projectw.domain.waiting.repository.WaitingHistoryRepository;
import com.projectw.security.AuthUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RedissonClient;
import org.redisson.client.protocol.ScoredEntry;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WaitingQueueServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private RedissonClient redissonClient;
    @Mock
    private NotificationService notificationService;
    @Mock
    private StoreRepository storeRepository;
    @Mock
    private RedisProducer redisProducer;
    @Mock
    private WaitingService waitingService;
    @Mock
    private WaitingHistoryService waitingHistoryService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private WaitingHistoryRepository waitingHistoryRepository;

    @InjectMocks
    private WaitingQueueService waitingQueueService;

    private AuthUser owner;
    private AuthUser user;
    private Store store;
    private Reservation reservation;
    private RScoredSortedSet sortedSetMock;
    private RAtomicLong atomicLong;

    @BeforeEach
    void setUp() {
        owner = new AuthUser(1L, "email", UserRole.ROLE_OWNER);
        user = new AuthUser(2L, "email2", UserRole.ROLE_USER);
        store = new Store();
        ReflectionTestUtils.setField(store, "id", 1L);
        ReflectionTestUtils.setField(store, "user", User.fromAuthUser(owner));
        sortedSetMock = mock(RScoredSortedSet.class);
        atomicLong = mock(RAtomicLong.class);
        reservation = mock(Reservation.class);
    }

    @Test
    void 커넥트_시_대기열_등록이_안돼있으면_ForbiddenException_발생() {
        given(sortedSetMock.rank(String.valueOf(owner.getUserId()))).willReturn(null);
        given(redissonClient.getScoredSortedSet(anyString())).willReturn(sortedSetMock);
        assertThatThrownBy(() -> {waitingQueueService.connect(owner, store.getId());})
                .isInstanceOf(ForbiddenException.class)
                .hasMessage(ResponseCode.FORBIDDEN.getMessage());
    }

    @Test
    void 커넥트() {
        // given
        SseEmitter result = new SseEmitter();
        given(redissonClient.getScoredSortedSet(anyString())).willReturn(sortedSetMock);
        given(sortedSetMock.rank(String.valueOf(owner.getUserId()))).willReturn(0);
        given(notificationService.subscribe(any(), any())).willReturn(result);

        // when
        var result2 = waitingQueueService.connect(owner, store.getId());

        // then
        assertThat(result2).isNotNull();
        verify(notificationService, times(1)).subscribe(anyString(), any());
    }

    @Test
    void 유저가_웨이팅_신청할때_가게가_존재하지_않으면_NotFOundException() {
        given(storeRepository.findWithUserById(store.getId())).willReturn(Optional.empty());

        assertThatThrownBy(() -> {waitingQueueService.addUserToQueue(user, store.getId());})
                .isInstanceOf(NotFoundException.class)
                .hasMessage(ResponseCode.NOT_FOUND_STORE.getMessage());
    }

    @Test
    void 유저가_이미_웨이팅_중인데_재등록_하면_UserAlreadyInQueueException() {
        given(storeRepository.findWithUserById(store.getId())).willReturn(Optional.of(store));
        given(waitingHistoryRepository.existsByUserAndStoreAndStatus(any(), any(), any())).willReturn(true);

        assertThatThrownBy(() -> {waitingQueueService.addUserToQueue(user, store.getId());})
                .isInstanceOf(UserAlreadyInQueueException.class)
                .hasMessage(ResponseCode.ALREADY_WAITING.getMessage());
    }

    @Test
    void 유저_대기열_등록() {
        // given
        given(storeRepository.findWithUserById(store.getId())).willReturn(Optional.of(store));
        given(waitingHistoryRepository.existsByUserAndStoreAndStatus(any(), any(), any())).willReturn(false);
        given(sortedSetMock.size()).willReturn(1);


        given(redissonClient.getScoredSortedSet(anyString())).willReturn(sortedSetMock);
        given(redissonClient.getAtomicLong(anyString())).willReturn(atomicLong);
        given(atomicLong.incrementAndGet()).willReturn(1L);

        // when
        WaitingQueueResponse.Info info = waitingQueueService.addUserToQueue(user, 1L);

        // then
        assertThat(info.rank()).isEqualTo(1);
        verify(waitingHistoryService, times(1)).createHistory(any(), any());
        verify(waitingService, times(1)).incrementWeight(anyString(), eq(1.0));
    }

    @Test
    void 웨이팅_체크인_시_가게가_없으면_NotFoundException() {
        // given
        given(storeRepository.findWithUserById(store.getId())).willReturn(Optional.empty());

        // when then
        assertThatThrownBy(() -> {waitingQueueService.pollFirstUser(owner, store.getId());})
                .isInstanceOf(NotFoundException.class)
                .hasMessage(ResponseCode.NOT_FOUND_STORE.getMessage());
    }

    @Test
    void 웨이팅_체크인_시_가게의_주인이_아니면_NotFoundException() {
        // given
        given(storeRepository.findWithUserById(1L)).willReturn(Optional.of(store));

        User u = User.fromAuthUser(new AuthUser(99L, "", UserRole.ROLE_OWNER));
        ReflectionTestUtils.setField(store, "user", u);

        // when then
        assertThatThrownBy(() -> {waitingQueueService.pollFirstUser(owner, store.getId());})
                .isInstanceOf(ForbiddenException.class)
                .hasMessage(ResponseCode.FORBIDDEN.getMessage());
    }

    @Test
    void 체크인_시_대기열_아무도_없으면_조기_리턴() {
        // given
        given(storeRepository.findWithUserById(anyLong())).willReturn(Optional.of(store));
        given(redissonClient.getScoredSortedSet(anyString())).willReturn(sortedSetMock);
        given(sortedSetMock.isEmpty()).willReturn(true);
        // when
        waitingQueueService.pollFirstUser(owner, store.getId());

        // then
        verify(sortedSetMock, times(0)).firstScore();
    }

    @Test
    void 웨이팅_체크인_시_유저가_없으면_NotFoundException() {
        // given
        given(storeRepository.findWithUserById(store.getId())).willReturn(Optional.of(store));
        given(sortedSetMock.isEmpty()).willReturn(false);
        given(sortedSetMock.pollFirst()).willReturn(String.valueOf(owner.getUserId()));
        given(redissonClient.getScoredSortedSet(anyString())).willReturn(sortedSetMock);
        given(sortedSetMock.firstScore()).willReturn(1.0);
        given(sortedSetMock.pollFirst()).willReturn("1");
        given(userRepository.findById(any())).willReturn(Optional.empty());

        // when then
        assertThatThrownBy(() -> {waitingQueueService.pollFirstUser(owner, store.getId());})
                .isInstanceOf(NotFoundException.class)
                .hasMessage(ResponseCode.NOT_FOUND_USER.getMessage());
    }

    @Test
    void 웨이팅_체크인() {
        // when
        given(storeRepository.findWithUserById(store.getId())).willReturn(Optional.of(store));
        given(sortedSetMock.isEmpty()).willReturn(false);
        given(sortedSetMock.pollFirst()).willReturn(String.valueOf(owner.getUserId()));
        given(redissonClient.getScoredSortedSet(anyString())).willReturn(sortedSetMock);
        given(userRepository.findById(any())).willReturn(Optional.of(User.fromAuthUser(user)));
        given(sortedSetMock.firstScore()).willReturn(1.0);
        given(sortedSetMock.pollFirst()).willReturn("1");

        // when
        waitingQueueService.pollFirstUser(owner, store.getId());

        // then
        verify(waitingHistoryService, times(1)).completeHistory(any(), eq(store));
        verify(waitingService, times(1)).incrementWeight(any(), eq(-1.0));
        verify(notificationService, times(1)).delete(anyString());
    }

    @Test
    void 유저가_웨이팅_취소시_가게가_없으면_NotFoundException() {
        // given
        given(storeRepository.findById(store.getId())).willReturn(Optional.empty());

        // when then
        assertThatThrownBy(() -> {waitingQueueService.cancel(owner, store.getId());})
                .isInstanceOf(NotFoundException.class)
                .hasMessage(ResponseCode.NOT_FOUND_STORE.getMessage());
    }


    @Test
    void 유저가_웨이팅_취소() {
        // given
        given(storeRepository.findById(store.getId())).willReturn(Optional.of(store));
        given(redissonClient.getScoredSortedSet(anyString())).willReturn(sortedSetMock);

        // when
        waitingQueueService.cancel(user, store.getId());

        // then
        verify(waitingHistoryService, times(1)).cancelHistory(any(User.class), eq(store));
        verify(sortedSetMock, times(1)).remove(String.valueOf(user.getUserId()));
        verify(waitingService, times(1)).incrementWeight(anyString(), eq(-1.0));
        verify(notificationService, times(1)).delete(anyString());
    }

    @Test
    void 특정_순위부터_대기열_삭제_시_가게가_없으면_NotFoundException() {
        // given
        given(storeRepository.findWithUserById(store.getId())).willReturn(Optional.empty());

        // when then
        assertThatThrownBy(() -> {waitingQueueService.clearQueueFromRank(owner, store.getId(), 1);})
                .isInstanceOf(NotFoundException.class)
                .hasMessage(ResponseCode.NOT_FOUND_STORE.getMessage());
    }

    @Test
    void 특정_순위부터_대기열_삭제_시_가게_사장이_아니면_ForbiddenException() {
        // given
        given(storeRepository.findWithUserById(store.getId())).willReturn(Optional.of(store));

        User u = User.fromAuthUser(new AuthUser(99L, "", UserRole.ROLE_OWNER));
        ReflectionTestUtils.setField(store, "user", u);

        // when then
        assertThatThrownBy(() -> {waitingQueueService.clearQueueFromRank(owner, store.getId(), 1);})
                .isInstanceOf(ForbiddenException.class)
                .hasMessage(ResponseCode.FORBIDDEN.getMessage());
    }


    @Test
    void 특정_순위부터_대기열_삭제() {
        // given
        given(storeRepository.findWithUserById(store.getId())).willReturn(Optional.of(store));
        given(redissonClient.getScoredSortedSet(anyString())).willReturn(sortedSetMock);
        given(sortedSetMock.valueRange(anyInt(), eq(-1))).willReturn(List.of("1", "2", "3"));

        // when
        waitingQueueService.clearQueueFromRank(owner, store.getId(), 1);

        // then
        verify(notificationService, times(3)).delete(anyString());
        verify(notificationService, times(3)).broadcast(anyString(), any());
        verify(sortedSetMock, times(1)).removeRangeByRank(1, -1);
    }

    @Test
    void 웨이팅_목록_조회_시_가게가_없으면_NotFoundException() {
        // given
        given(storeRepository.findWithUserById(store.getId())).willReturn(Optional.empty());

        // when then
        assertThatThrownBy(() -> {waitingQueueService.getWaitingList(owner, store.getId());})
                .isInstanceOf(NotFoundException.class)
                .hasMessage(ResponseCode.NOT_FOUND_STORE.getMessage());
    }

    @Test
    void 웨이팅_목록_조회_시_가게_사장이_아니면_ForbiddenException() {
        // given
        given(storeRepository.findWithUserById(store.getId())).willReturn(Optional.of(store));

        User u = User.fromAuthUser(new AuthUser(99L, "", UserRole.ROLE_OWNER));
        ReflectionTestUtils.setField(store, "user", u);

        // when then
        assertThatThrownBy(() -> {waitingQueueService.getWaitingList(owner, store.getId());})
                .isInstanceOf(ForbiddenException.class)
                .hasMessage(ResponseCode.FORBIDDEN.getMessage());
    }

    @Test
    void 웨이팅_목록_조회_Sorted_set이_비어있으면_빈_컬렉션_반환() {
        // given
        given(storeRepository.findWithUserById(store.getId())).willReturn(Optional.of(store));
        given(redissonClient.getScoredSortedSet(anyString())).willReturn(sortedSetMock);
        given(sortedSetMock.isEmpty()).willReturn(true);

        // when
        WaitingQueueResponse.List waitingList = waitingQueueService.getWaitingList(owner, store.getId());

        // then
        assertThat(waitingList).isNotNull();
        assertThat(waitingList.totalWaitingNumber()).isEqualTo(0);
        assertThat(waitingList.userIds()).hasSize(0);
    }

    @Test
    void 웨이팅_목록_조회() {
        // given
        given(storeRepository.findWithUserById(store.getId())).willReturn(Optional.of(store));
        given(redissonClient.getScoredSortedSet(anyString())).willReturn(sortedSetMock);
        given(sortedSetMock.isEmpty()).willReturn(false);
        given(sortedSetMock.entryRange(0, -1)).willReturn(List.of(
                new ScoredEntry<>(1.0, "2"),
                new ScoredEntry<>(2.0, "3")
        ));

        // when
        WaitingQueueResponse.List waitingList = waitingQueueService.getWaitingList(owner, store.getId());

        // then
        assertThat(waitingList.totalWaitingNumber()).isEqualTo(2);
        assertThat(waitingList.userIds()).hasSize(2);
    }

    @Test
    void 웨이팅_대기열에_등록_여부_확인() {
        // given
        given(storeRepository.findById(store.getId())).willReturn(Optional.of(store));
        given(waitingHistoryRepository.existsByUserAndStoreAndStatus(any(), any(), any())).willReturn(true);

        // when
        WaitingQueueResponse.WaitingInfo waitingInfo = waitingQueueService.checkWaitingStatus(user, store.getId());

        // then
        assertThat(waitingInfo.isWaiting()).isTrue();
    }

    @Test
    void 웨이팅_대기열에_미등록_여부_확인() {
        // given
        given(storeRepository.findById(store.getId())).willReturn(Optional.of(store));
        given(waitingHistoryRepository.existsByUserAndStoreAndStatus(any(), any(), any())).willReturn(false);

        // when
        WaitingQueueResponse.WaitingInfo waitingInfo = waitingQueueService.checkWaitingStatus(user, store.getId());

        // then
        assertThat(waitingInfo.isWaiting()).isFalse();
    }

}
