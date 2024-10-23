package com.projectw.domain.store.repository;

import com.projectw.domain.store.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface StoreRepository extends JpaRepository<Store, Long>, StoreDslRepository{
    @Query("SELECT COUNT(s) FROM Store s " +
            "WHERE s.id = :id AND " +
            "((s.openTime <= CURRENT_TIME AND s.closeTime >= CURRENT_TIME AND s.closeTime > s.openTime) " +  // 같은 날에 문을 닫는 경우
            "OR (s.openTime <= CURRENT_TIME OR s.closeTime >= CURRENT_TIME AND s.closeTime < s.openTime))")  // 다음 날까지 문을 여는 경우
    long countStoresOpenNow(Long id);
}
