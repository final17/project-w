package com.projectw.domain.waiting.service;

import com.projectw.common.enums.ResponseCode;
import com.projectw.common.exceptions.AccessDeniedException;
import com.projectw.common.exceptions.InvalidRequestException;
import com.projectw.common.exceptions.NotFoundException;
import com.projectw.domain.store.entity.Store;
import com.projectw.domain.store.repository.StoreRepository;
import com.projectw.domain.waiting.dto.WaitingStatisticsResponse;
import com.projectw.domain.waiting.entity.DailyWaitingStatistics;
import com.projectw.domain.waiting.entity.HourlyWaitingStatistics;
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
import java.util.ArrayList;
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
     *
     * @param user
     * @param storeId
     * @return
     */
    public WaitingStatisticsResponse.Daily getDailyWaitingStatistics(AuthUser user, long storeId, LocalDate date) {
        Store store = findByStoreIdAndOwnerCheck(user, storeId);
        checkValidDateRangeForStatistics(store, date);
        return dailyWaitingStatisticsRepository.findAllByStoreAndDate(store, date).map(WaitingStatisticsResponse.Daily::new).orElseGet(WaitingStatisticsResponse.Daily::emptyData);
    }

    /**
     * 해당가게의 시작일 ~ 종료일 까지의 일 데이터 반환
     *
     * @param user
     * @param storeId
     * @param startDate
     * @param endDate
     * @param page
     * @param size
     * @return
     */
    public WaitingStatisticsResponse.DailyPage getDailyWaitingStatisticsBetweenDate(AuthUser user, long storeId, LocalDate startDate, LocalDate endDate, int page, int size) {
        Store store = findByStoreIdAndOwnerCheck(user, storeId);
        Pageable pageable = PageRequest.of(page - 1, size);

        // 시작일은 가게 오픈일 이후여야 한다.
        // 종료일은 어제까지
        checkValidDateRangeForStatistics(store, startDate, endDate);

        Page<DailyWaitingStatistics> result = dailyWaitingStatisticsRepository.findAllByStoreAndDateBetweenOrderByDateAsc(store, startDate, endDate, pageable);

        List<WaitingStatisticsResponse.Daily> contents = result.getContent()
                .stream()
                .map(WaitingStatisticsResponse.Daily::new)
                .toList();

        return new WaitingStatisticsResponse
                .DailyPage(pageable.getPageNumber(), pageable.getPageSize(), contents.size(), result.getTotalElements(), result.getTotalPages(), result.hasNext(), contents);
    }

    /**
     * 해당 가게의 해당 일의 시간대별 통계 데이터 목록 반환
     *
     * @param user
     * @param storeId
     * @param date
     * @return
     */
    public List<WaitingStatisticsResponse.Hourly> getHourlyWaitingStatistics(AuthUser user, long storeId, LocalDate date) {
        return getHourlyWaitingStatisticsByHourRange(user, storeId, date, 0, 23);
    }

    /**
     * 해당 가게의 해당 일의 start시 부터 end시까지 시간대별 통계 데이터 목록 반환
     *
     * @param user
     * @param storeId
     * @param date
     * @return
     */
    public List<WaitingStatisticsResponse.Hourly> getHourlyWaitingStatisticsByHourRange(AuthUser user, long storeId, LocalDate date, int startHour, int endHour) {
        if (startHour < 0 || endHour < 0 || startHour > 23 || endHour > 23) {
            throw new InvalidRequestException(ResponseCode.INVALID_TIME_RANGE);
        }

        Store store = findByStoreIdAndOwnerCheck(user, storeId);

        checkValidDateRangeForStatistics(store, date);

        List<HourlyWaitingStatistics> find = hourlyWaitingStatisticsRepository.findAllByStoreAndDateAndHourBetweenOrderByHour(store, date, startHour, endHour);

        // start 시 부터 end 시 까지
        List<WaitingStatisticsResponse.Hourly> results = new ArrayList<>();
        for (int i = startHour; i <= endHour; ++i) {
            int hour = i;
            if (find.stream().noneMatch(x -> x.getHour() == hour)) {
                results.add(WaitingStatisticsResponse.Hourly.emptyData(hour));
            }
        }

        results.addAll(find.stream().map(WaitingStatisticsResponse.Hourly::new).toList());
        results.sort((x, y) -> Integer.compare(x.hour(), y.hour()));
        return results;
    }

    /**
     * 시작일은 가게 오픈일 이후여야 한다.
     * 종료일은 어제까지
     * @param store
     * @param date
     */
    private void checkValidDateRangeForStatistics(Store store, LocalDate date) {
        if (date.isBefore(store.getCreatedAt().toLocalDate()) || date.isAfter(LocalDate.now().minusDays(1))) {
            throw new InvalidRequestException(ResponseCode.INVALID_WAITING_STATISTICS_DATE_RANGE);
        }
    }

    /**
     * 시작일은 가게 오픈일 이후여야 한다.
     * 종료일은 어제까지
     * @param store
     * @param s
     * @param e
     */
    private void checkValidDateRangeForStatistics(Store store, LocalDate s, LocalDate e) {
        checkValidDateRangeForStatistics(store, s);
        checkValidDateRangeForStatistics(store, e);

        if (s.isAfter(e)) {
            throw new InvalidRequestException(ResponseCode.START_DATE_AFTER_END_DATE);
        }
    }

    private Store findByStoreIdAndOwnerCheck(AuthUser user, long storeId) {
        Store store = storeRepository.findWithUserById(storeId)
                .orElseThrow(() -> new NotFoundException(ResponseCode.NOT_FOUND_STORE));

        if(!store.getUser().getId().equals(user.getUserId())) {
            throw new AccessDeniedException(ResponseCode.FORBIDDEN);
        }

        return store;
    }
}
