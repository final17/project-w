package com.projectw.domain.waiting.repository;

import com.projectw.domain.store.entity.Store;
import com.projectw.domain.waiting.entity.DailyWaitingStatistics;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailyWaitingStatisticsRepository extends JpaRepository<DailyWaitingStatistics, Long> {

    Optional<DailyWaitingStatistics> findAllByStoreAndDate(Store store, LocalDate date);
    Page<DailyWaitingStatistics> findAllByStoreAndDateBetweenOrderByDateAsc(Store store, LocalDate start, LocalDate end, Pageable pageable);

    @Query("SELECT d " +
            "FROM DailyWaitingStatistics d " +
            "WHERE d.store=:store AND " +
            "FUNCTION('YEAR', d.date)=:year AND " +
            "FUNCTION('MONTH', d.date)=:month " +
            "ORDER BY d.date ")
    List<DailyWaitingStatistics>  findAllByStoreMonthly(@Param("store") Store store, @Param("year") int year, @Param("month") int month);
}
