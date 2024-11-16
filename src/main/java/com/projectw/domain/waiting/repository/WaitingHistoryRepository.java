package com.projectw.domain.waiting.repository;

import com.projectw.domain.store.entity.Store;
import com.projectw.domain.user.entity.User;
import com.projectw.domain.waiting.entity.WaitingHistory;
import com.projectw.domain.waiting.enums.WaitingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface WaitingHistoryRepository extends JpaRepository<WaitingHistory, Long> {

    @Query("SELECT w FROM WaitingHistory w WHERE w.store=:store AND w.user=:user AND w.status=:status")
    Optional<WaitingHistory> findHistoryByStatus(@Param("user") User user, @Param("store") Store store, @Param("status") WaitingStatus status);

    @Query("SELECT w FROM WaitingHistory w WHERE w.store=:store AND w.user.id in (:userIds) AND w.status=:status")
    List<WaitingHistory> findAllHistoryByStatus(@Param("userIds") List<Long> userIds, @Param("store") Store store, @Param("status") WaitingStatus status);

    @Query("SELECT w FROM WaitingHistory w INNER JOIN FETCH w.store WHERE w.user=:user AND w.status=:status AND w.store.isDeleted = false")
    List<WaitingHistory> findStoreListByStatus(@Param("user") User user, @Param("status") WaitingStatus status);

    boolean existsByUserAndStoreAndStatus(User user, Store store, WaitingStatus status);
}
