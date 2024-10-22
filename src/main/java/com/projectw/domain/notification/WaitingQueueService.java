package com.projectw.domain.notification;

import com.projectw.common.annotations.RedisLock;
import com.projectw.common.enums.ResponseCode;
import com.projectw.common.exceptions.ForbiddenException;
import com.projectw.common.exceptions.UserAlreadyInQueueException;
import com.projectw.domain.notification.service.SseNotificationService;
import com.projectw.domain.store.repository.StoreRepository;
import com.projectw.security.AuthUser;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RedissonClient;
import org.redisson.client.protocol.ScoredEntry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class WaitingQueueService {
    private final RedissonClient redissonClient;
    private final SseNotificationService notificationService;
    private final StoreRepository storeRepository;


    public SseEmitter connect(AuthUser authUser, long storeId) {
        RScoredSortedSet<String> sortedSet = redissonClient.getScoredSortedSet(getRedisSortedSetKey(storeId));
        Integer rank = sortedSet.rank(String.valueOf(authUser.getUserId()));
        if(rank == null) {
            // 대기열에 등록부터 해야함
            throw new ForbiddenException(ResponseCode.FORBIDDEN);
        }

        Object data = rank + 1;
        return notificationService.subscribe(getSseKey(storeId, String.valueOf(authUser.getUserId())), data);
    }

    @RedisLock("#storeId")
    public void addUserToQueue(AuthUser authUser, long storeId){

        RScoredSortedSet<String> sortedSet = redissonClient.getScoredSortedSet(getRedisSortedSetKey(storeId));
        Integer rank = sortedSet.rank(String.valueOf(authUser.getUserId()));

        if(rank != null) {
            throw new UserAlreadyInQueueException(ResponseCode.ALREADY_WAITING);
        }

        // 현재 시간을 score로 설정하여 대기열에 추가
        double score = Instant.now().getEpochSecond();  // UNIX 타임스탬프 사용
        sortedSet.add(score, String.valueOf(authUser.getUserId()));
        updateAllUsers(storeId);
    }

    /**
     * 웨이팅 1번 받음
     */
    // @RedisLock("#storeId")
    public void popFirstUser(AuthUser authUser, long storeId) {

        RScoredSortedSet<String> sortedSet = redissonClient.getScoredSortedSet(getRedisSortedSetKey(storeId));
        if(sortedSet.isEmpty()) {
           return;
        }

//        Store store = storeRepository.findWithUserById(storeId)
//                .orElseThrow(() -> new NotFoundException(ResponseCode.NOT_FOUND_STORE));
//
//        // 가게의 주인이 아니면
//        if(!store.getUser().getId().equals(authUser.getUserId())) {
//            throw new ForbiddenException(ResponseCode.FORBIDDEN);
//        }


        String popUserId = sortedSet.pollFirst();
        notificationService.delete(getSseKey(storeId, popUserId));
        updateAllUsers(storeId);
    }

    /**
     * 유저가 웨이팅 취소
     */
    @RedisLock("#storeId")
    public void cancel(AuthUser authUser, long storeId) {
        RScoredSortedSet<String> sortedSet = redissonClient.getScoredSortedSet(getRedisSortedSetKey(storeId));
        sortedSet.remove(String.valueOf(authUser.getUserId()));
        notificationService.delete(getSseKey(storeId, String.valueOf(authUser.getUserId())));
        updateAllUsers(storeId);
    }

    /**
     * cutline 뒤에 번호부터 웨이팅 취소처리
     */
    public void clearQueueFromRank(AuthUser authUser, long storeId, int cutline) {
        RScoredSortedSet<String> sortedSet = redissonClient.getScoredSortedSet(getRedisSortedSetKey(storeId));
        cutline = Math.max(cutline, 0);

        // startRank부터 끝까지 삭제 (-1은 마지막 요소까지를 의미)
        // 마감 메세지 보내기
        Collection<String> values = sortedSet.valueRange(cutline, -1);
        for (String userId : values) {
            notificationService.broadcast(getSseKey(storeId, userId), "대기열 마감");
            notificationService.delete(getSseKey(storeId, userId));
        }

        // cutline이 50이면 50 뒤부터 삭제
        sortedSet.removeRangeByRank(cutline, -1);
    }

    /**
     * 해당 가게의 웨이팅 중인 웨이팅번호, 유저 아이디 조회
     */
    public WaitingQueueResponse.List getWaitingList(AuthUser authUser, long storeId) {
        RScoredSortedSet<String> sortedSet = redissonClient.getScoredSortedSet(getRedisSortedSetKey(storeId));
        if(sortedSet.isEmpty()) {
            return new WaitingQueueResponse.List(0, List.of());
        }

        Collection<ScoredEntry<String>> scoredEntries = sortedSet.entryRange(0, -1);
        List<WaitingQueueResponse.Info> ids = new ArrayList<>();
        int rank = 1;
        for(ScoredEntry<String> scoredEntry : scoredEntries) {
            ids.add(new WaitingQueueResponse.Info(rank++, scoredEntry.getValue()));
        }

        return new WaitingQueueResponse.List(ids.size(), ids);
    }

    /**
     * 대기번호 최신화
     */
    @RedisLock("#storeId")
    private void updateAllUsers(long storeId) {
        RScoredSortedSet<String> sortedSet = redissonClient.getScoredSortedSet(getRedisSortedSetKey(storeId));
        Collection<String> values = sortedSet.valueRange(0, -1);
        int rank = 1;

        for (String userId : values ) {
            notificationService.broadcast(getSseKey(storeId, userId), rank++);
        }
    }

    private String getRedisSortedSetKey(long storeId) {
        return "waitingQueue:store:" + storeId + ":user:";
    }

    private String getSseKey(long storeId, String userId) {
        return "waitingQueue:store:" + storeId + ":user:" + userId;
    }
}
