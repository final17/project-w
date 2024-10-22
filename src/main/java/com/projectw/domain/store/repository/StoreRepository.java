package com.projectw.domain.store.repository;

import com.projectw.domain.store.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface StoreRepository extends JpaRepository<Store, Long>, StoreDslRepository{
    @Query("SELECT COUNT(s) FROM Store s " +
            "WHERE s.id = :id AND " +
            "((s.open <= CURRENT_TIME AND s.close >= CURRENT_TIME AND s.close > s.open) " +  // 같은 날에 문을 닫는 경우
            "OR (s.open <= CURRENT_TIME OR s.close >= CURRENT_TIME AND s.close < s.open))")  // 다음 날까지 문을 여는 경우
    long countStoresOpenNow(Long id);
}
