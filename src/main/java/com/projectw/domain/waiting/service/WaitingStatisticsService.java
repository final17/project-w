package com.projectw.domain.waiting.service;

import com.projectw.common.enums.ResponseCode;
import com.projectw.common.exceptions.NotFoundException;
import com.projectw.domain.store.entity.Store;
import com.projectw.domain.store.repository.StoreRepository;
import com.projectw.domain.waiting.dto.WaitingStatisticsResponse;
import com.projectw.domain.waiting.entity.DailyWaitingStatistics;
import com.projectw.domain.waiting.repository.DailyWaitingStatisticsRepository;
import com.projectw.domain.waiting.repository.HourlyWaitingStatisticsRepository;
import com.projectw.security.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class WaitingStatisticsService {

    private final DailyWaitingStatisticsRepository dailyWaitingStatisticsRepository;
    private final HourlyWaitingStatisticsRepository hourlyWaitingStatisticsRepository;
    private final StoreRepository storeRepository;

    /**
     * 해당 가게의 해당 일 통계 가져오기
     * @param user
     * @param storeId
     * @return
     */
    public WaitingStatisticsResponse.Daily getDailyWaitingStatistics(AuthUser user, long storeId, LocalDate date) {

        Store store = findByStoreId(storeId);
        DailyWaitingStatistics find = dailyWaitingStatisticsRepository.findAllByStoreAndDate(store, date)
                .orElseThrow(() -> new NotFoundException(ResponseCode.NOT_FOUND_DAILY_WAITING_STATISTICS));

        return new WaitingStatisticsResponse.Daily(find);
    }

    /**
     * 해당가게의 시작일 ~ 종료일 까지의 일 데이터 반환
     * @param user
     * @param storeId
     * @param startDate
     * @param endDate
     * @param page
     * @param size
     * @return
     */
    public WaitingStatisticsResponse.DailyPage getDailyWaitingStatisticsBetweenDate(AuthUser user, long storeId, LocalDate startDate, LocalDate endDate, int page, int size) {
        Store store = findByStoreId(storeId);
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<DailyWaitingStatistics> result = dailyWaitingStatisticsRepository.findAllByStoreAndDateBetweenOrderByDateAsc(store, startDate, endDate, pageable);

        List<WaitingStatisticsResponse.Daily> contents = result.getContent()
                .stream()
                .map(WaitingStatisticsResponse.Daily::new)
                .toList();

        return new WaitingStatisticsResponse.DailyPage(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                contents.size(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.hasNext(),
                contents
        );
    }

    //todo 시간 별 데이터 반환

    private Store findByStoreId(long storeId) {
        return storeRepository.findById(storeId)
                .orElseThrow(()-> new NotFoundException(ResponseCode.NOT_FOUND_STORE));
    }
}
