package com.projectw.domain.waiting.repository;

import com.projectw.domain.store.entity.Store;
import com.projectw.domain.waiting.entity.DailyWaitingStatistics;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface DailyWaitingStatisticsRepository extends JpaRepository<DailyWaitingStatistics, Long> {

    Optional<DailyWaitingStatistics> findAllByStoreAndDate(Store store, LocalDate date);
    Page<DailyWaitingStatistics> findAllByStoreAndDateBetweenOrderByDateAsc(Store store, LocalDate start, LocalDate end, Pageable pageable);
}
