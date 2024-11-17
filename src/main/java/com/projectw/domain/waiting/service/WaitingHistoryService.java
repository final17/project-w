package com.projectw.domain.waiting.service;

import com.projectw.common.enums.ResponseCode;
import com.projectw.common.exceptions.NotFoundException;
import com.projectw.domain.store.entity.Store;
import com.projectw.domain.user.entity.User;
import com.projectw.domain.waiting.dto.WaitingQueueResponse;
import com.projectw.domain.waiting.entity.QWaitingHistory;
import com.projectw.domain.waiting.entity.WaitingHistory;
import com.projectw.domain.waiting.enums.WaitingStatus;
import com.projectw.domain.waiting.repository.WaitingHistoryRepository;
import com.projectw.security.AuthUser;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class WaitingHistoryService {

    private final WaitingHistoryRepository waitingHistoryRepository;
    private final JPAQueryFactory jpaQueryFactory;

    public void createHistory(User user, Store store) {
        // 오늘 날짜 기준으로 웨이팅 기록 가져오기
        waitingHistoryRepository.save(new WaitingHistory(user, store));
    }

    public void cancelHistory(User user, Store store) {
        WaitingHistory history = waitingHistoryRepository.findHistoryByStatus(user, store, WaitingStatus.REGISTERED)
                .orElseThrow(()-> new NotFoundException(ResponseCode.NOT_FOUND_WAITING_HISTORY));

        history.setCanceled();
    }

    public void cancelHistory(List<Long> userIds, Store store) {
        // bulkUpdate
        LocalDateTime now = LocalDateTime.now();
        long updatedCount = jpaQueryFactory.update(QWaitingHistory.waitingHistory)
                .set(QWaitingHistory.waitingHistory.status, WaitingStatus.CLOSED)
                .set(QWaitingHistory.waitingHistory.canceledAt, now)
                .set(QWaitingHistory.waitingHistory.waitingTime, Expressions.numberTemplate(Long.class,
                        "TIMESTAMPDIFF(MINUTE, {0}, {1})",
                        QWaitingHistory.waitingHistory.registeredAt,
                        now))
                .where(QWaitingHistory.waitingHistory.user.id.in(userIds)
                        .and(QWaitingHistory.waitingHistory.store.eq(store))
                        .and(QWaitingHistory.waitingHistory.status.eq(WaitingStatus.REGISTERED)))
                .execute();
    }

    public void completeHistory(User user, Store store) {
        WaitingHistory history = waitingHistoryRepository.findHistoryByStatus(user, store, WaitingStatus.REGISTERED)
                .orElseThrow(()-> new NotFoundException(ResponseCode.NOT_FOUND_WAITING_HISTORY));

        history.setComplete();
    }

    public WaitingQueueResponse.MyWaitingStoreList getRegisteredStoreList(AuthUser user) {
        List<WaitingHistory> find = waitingHistoryRepository.findStoreListByStatus(User.fromAuthUser(user), WaitingStatus.REGISTERED);

        return new WaitingQueueResponse.MyWaitingStoreList(find.stream()
                .map(x-> x
                        .getStore()
                        .getId())
                .toList());
    }
}
