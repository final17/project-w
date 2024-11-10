package com.projectw.domain.waiting.repository;

import com.projectw.domain.store.entity.Store;
import com.projectw.domain.waiting.entity.HourlyWaitingStatistics;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface HourlyWaitingStatisticsRepository extends JpaRepository<HourlyWaitingStatistics, Long> {

    List<HourlyWaitingStatistics> findAllByStoreAndDateOrderByHour(Store store, LocalDate date);
    List<HourlyWaitingStatistics> findAllByStoreAndDateAndHourBetweenOrderByHour(Store store, LocalDate date, int startHour, int endHour);
    Optional<HourlyWaitingStatistics> findByStoreAndDateAndHourOrderByDateDescHourAsc(Store store, LocalDate date, int hour);
    List<HourlyWaitingStatistics> findAllByStoreAndHourBetweenOrderByDateDescHourAsc(Store store, int startHour, int endHour);
}
