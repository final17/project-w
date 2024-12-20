package com.projectw.domain.waiting.service;

import com.projectw.common.annotations.RedisLock;
import com.projectw.common.enums.ResponseCode;
import com.projectw.common.exceptions.ForbiddenException;
import com.projectw.common.exceptions.NotFoundException;
import com.projectw.common.exceptions.UserAlreadyInQueueException;
import com.projectw.common.utils.RedisProducer;
import com.projectw.domain.notification.service.NotificationService;
import com.projectw.domain.reservation.entity.Reservation;
import com.projectw.domain.reservation.enums.ReservationStatus;
import com.projectw.domain.reservation.enums.ReservationType;
import com.projectw.domain.reservation.exception.StoreNotOpenException;
import com.projectw.domain.reservation.repository.ReservationRepository;
import com.projectw.domain.reservation.service.ReservationService;
import com.projectw.domain.store.entity.Store;
import com.projectw.domain.store.repository.StoreRepository;
import com.projectw.domain.user.entity.User;
import com.projectw.domain.user.repository.UserRepository;
import com.projectw.domain.waiting.dto.WaitingQueueResponse;
import com.projectw.domain.waiting.enums.WaitingStatus;
import com.projectw.domain.waiting.repository.WaitingHistoryRepository;
import com.projectw.security.AuthUser;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RedissonClient;
import org.redisson.client.protocol.ScoredEntry;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class WaitingQueueService {

    private final RedissonClient redissonClient;
    private final NotificationService notificationService;
    private final StoreRepository storeRepository;
    private final WaitingService waitingService;
    private final WaitingHistoryService waitingHistoryService;
    private final UserRepository userRepository;
    private final WaitingHistoryRepository waitingHistoryRepository;
    private final ReservationService reservationService;
    private final ReservationRepository reservationRepository;

    public SseEmitter connect(AuthUser authUser, long storeId) {
        if(authUser == null) return null;

        RScoredSortedSet<String> sortedSet = redissonClient.getScoredSortedSet(getRedisSortedSetKey(storeId));
        Integer rank = sortedSet.rank(String.valueOf(authUser.getUserId()));
        if(rank == null) {
            // 대기열에 등록부터 해야함
            throw new ForbiddenException(ResponseCode.FORBIDDEN);
        }

        Object data = rank + 1;
        return notificationService.subscribe(getSseKey(storeId, String.valueOf(authUser.getUserId())), data);
    }

    @RedisLock(value = "#storeId")
    public WaitingQueueResponse.Info addUserToQueue(AuthUser authUser, long storeId){

        Store store = storeRepository.findWithUserById(storeId)
                .orElseThrow(() -> new NotFoundException(ResponseCode.NOT_FOUND_STORE));

        if( !(LocalTime.now().isAfter(store.getOpenTime()) && LocalTime.now().isBefore(store.getCloseTime())) ) {
            throw new StoreNotOpenException(ResponseCode.STORE_NOT_OPEN);
        }

        // REGISTERED 상태가 존재하면 이미 웨이팅 중
        if(waitingHistoryRepository.existsByUserAndStoreAndStatus(User.fromAuthUser(authUser), store, WaitingStatus.REGISTERED)){
            throw new UserAlreadyInQueueException(ResponseCode.ALREADY_WAITING);
        }

        // 웨이팅 기록 생성
        waitingHistoryService.createHistory(User.fromAuthUser(authUser), store);

        RScoredSortedSet<String> sortedSet = redissonClient.getScoredSortedSet(getRedisSortedSetKey(storeId));

        // 발권 번호를 score로 설정하여 대기열에 추가
        long score = redissonClient.getAtomicLong(getRedisWaitingNumKey(storeId)).incrementAndGet();
        sortedSet.add(score, String.valueOf(authUser.getUserId()));
        // 웨이팅 서비스에서 레스토랑의 가중치 증가
        waitingService.incrementWeight(String.valueOf(storeId), 1.0);
        updateAllUsers(storeId);

        return new WaitingQueueResponse.Info(sortedSet.size(), authUser.getUserId());
    }

    /**
     * 웨이팅 1번 받음
     */
    @RedisLock("#storeId")
    public void pollFirstUser(AuthUser authUser, long storeId) {

        Store store = storeRepository.findWithUserById(storeId)
                .orElseThrow(() -> new NotFoundException(ResponseCode.NOT_FOUND_STORE));

        // 가게의 주인이 아니면
        if(!store.getUser().getId().equals(authUser.getUserId())) {
            throw new ForbiddenException(ResponseCode.FORBIDDEN);
        }

        RScoredSortedSet<String> sortedSet = redissonClient.getScoredSortedSet(getRedisSortedSetKey(storeId));
        if(sortedSet.isEmpty()) {
           return;
        }


        Double score = sortedSet.firstScore();
        String popUserId = sortedSet.pollFirst();

        User user = userRepository.findById(Long.parseLong(popUserId))
                .orElseThrow(() -> new NotFoundException(ResponseCode.NOT_FOUND_USER));

        // 완료로 변경
        waitingHistoryService.completeHistory(user, store);

        // 웨이팅 서비스에서 레스토랑 가중치 감소
        waitingService.incrementWeight(String.valueOf(storeId), -1.0);

        LocalDateTime at = LocalDateTime.now();
        Long num = score.longValue();

        Reservation reservation = Reservation.builder()
                .orderId(UUID.randomUUID().toString())
                .status(ReservationStatus.COMPLETE)
                .type(ReservationType.WAIT)          // 웨이팅 , 예약 중 예약이라는 의미
                .reservationDate(at.toLocalDate())
                .reservationTime(at.toLocalTime().truncatedTo(TimeUnit.SECONDS.toChronoUnit()))
                .numberPeople(1L)
                .reservationNo(num)
                .paymentYN(false)
                .paymentAmt(0L)
                .user(user)
                .store(store)
                .build();

        reservationRepository.save(reservation);

        notificationService.broadcast(getSseKey(storeId, popUserId), 0);
        notificationService.delete(getSseKey(storeId,popUserId));
        updateAllUsers(storeId);
    }

    /**
     * 유저가 웨이팅 취소
     */
    @RedisLock("#storeId")
    public void cancel(AuthUser authUser, long storeId) {

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new NotFoundException(ResponseCode.NOT_FOUND_STORE));

        waitingHistoryService.cancelHistory(User.fromAuthUser(authUser), store);

        RScoredSortedSet<String> sortedSet = redissonClient.getScoredSortedSet(getRedisSortedSetKey(storeId));
        sortedSet.remove(String.valueOf(authUser.getUserId()));

        // 웨이팅 서비스에서 레스토랑 가중치 감소
        waitingService.incrementWeight(String.valueOf(storeId), -1.0);

        updateAllUsers(storeId);
        notificationService.broadcast(getSseKey(storeId, String.valueOf(authUser.getUserId())), "cancel");
        notificationService.delete(getSseKey(storeId, String.valueOf(authUser.getUserId())));
    }

    /**
     * cutline 뒤에 번호부터 웨이팅 취소처리
     */
    @RedisLock("#storeId")
    public void clearQueueFromRank(AuthUser authUser, long storeId, int cutline) {

        Store store = storeRepository.findWithUserById(storeId)
                .orElseThrow(() -> new NotFoundException(ResponseCode.NOT_FOUND_STORE));

        if(!store.getUser().getId().equals(authUser.getUserId())) {
            throw new ForbiddenException(ResponseCode.FORBIDDEN);
        }


        RScoredSortedSet<String> sortedSet = redissonClient.getScoredSortedSet(getRedisSortedSetKey(storeId));
        cutline = Math.max(cutline, 0);

        // startRank부터 끝까지 삭제 (-1은 마지막 요소까지를 의미)
        // 마감 메세지 보내기
        Collection<String> values = sortedSet.valueRange(cutline, -1);
        for (String userId : values) {
            notificationService.broadcast(getSseKey(storeId, userId), "end");
            notificationService.delete(getSseKey(storeId, userId));
        }


        List<Long> users = userRepository.findAllById(values.stream()
                        .map(Long::parseLong)
                        .toList())
                .stream()
                .map(User::getId).toList();

        waitingHistoryService.cancelHistory(users, store);
        // cutline이 50이면 50 뒤부터 삭제
        sortedSet.removeRangeByRank(cutline, -1);
    }

    /**
     * 해당 가게의 웨이팅 중인 웨이팅번호, 유저 아이디 조회
     */
    public WaitingQueueResponse.List getWaitingList(AuthUser authUser, long storeId) {
        Store store = storeRepository.findWithUserById(storeId)
                .orElseThrow(() -> new NotFoundException(ResponseCode.NOT_FOUND_STORE));

        if(!store.getUser().getId().equals(authUser.getUserId())) {
            throw new ForbiddenException(ResponseCode.FORBIDDEN);
        }

        RScoredSortedSet<String> sortedSet = redissonClient.getScoredSortedSet(getRedisSortedSetKey(storeId));
        if(sortedSet.isEmpty()) {
            return new WaitingQueueResponse.List(0, List.of());
        }

        Collection<ScoredEntry<String>> scoredEntries = sortedSet.entryRange(0, -1);
        List<WaitingQueueResponse.Info> ids = new ArrayList<>();
        int rank = 1;
        for(ScoredEntry<String> scoredEntry : scoredEntries) {
            ids.add(new WaitingQueueResponse.Info(rank++, Long.parseLong(scoredEntry.getValue())));

            // 상위 10명 까지만
            if(rank == 11){
                break;
            }
        }

        return new WaitingQueueResponse.List(scoredEntries.size(), ids);
    }

    /**
     * 대기번호 최신화
     */
    private void updateAllUsers(long storeId) {
        RScoredSortedSet<String> sortedSet = redissonClient.getScoredSortedSet(getRedisSortedSetKey(storeId));
        Collection<String> values = sortedSet.valueRange(0, -1);
        int rank = 1;

        for (String userId : values ) {
            notificationService.broadcast(getSseKey(storeId, userId), rank++);
        }
    }

    private String getRedisWaitingNumKey(long storeId) {return "waiting:store:" + storeId;}
    private String getRedisSortedSetKey(long storeId) {
        return "waitingQueue:store:" + storeId + ":user:";
    }

    private String getSseKey(long storeId, String userId) {
        return "waitingQueue:store:" + storeId + ":user:" + userId;
    }

    /**
     * 웨이팅 대기열에 들어가 있는지 확인
     * @param authUser
     * @param storeId
     * @return
     */
    public WaitingQueueResponse.WaitingInfo checkWaitingStatus(AuthUser authUser, long storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new NotFoundException(ResponseCode.NOT_FOUND_STORE));

        return new WaitingQueueResponse.WaitingInfo(waitingHistoryRepository.existsByUserAndStoreAndStatus(User.fromAuthUser(authUser), store, WaitingStatus.REGISTERED));
    }

}
