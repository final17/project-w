package com.projectw.domain.waiting.scheduler;

import com.projectw.domain.store.entity.Store;
import com.projectw.domain.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RedissonClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class WaitingNumResetScheduler {

    private final RedissonClient redissonClient;
    private final StoreRepository storeRepository;
    
    // 매일 자정 발권 번호 초기화
    @Scheduled(cron = "0 0 0 * * *")
    public void reset() {
        List<Store> all = storeRepository.findAll();
        for (Store store : all) {
            redissonClient.getAtomicLong("waiting:store:" + store.getId()).set(0L);
            redissonClient.getScoredSortedSet("waitingQueue:store:" + store.getId() + ":user:").clear();
        }
    }
}
