package com.projectw.domain.store.repository;

import com.projectw.domain.store.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface StoreRepository extends JpaRepository<Store, Long>, StoreDslRepository{
    @Query("SELECT COUNT(s) FROM Store s " +
            "WHERE s.id = :id AND s.open <= CURRENT_TIME AND s.close >= CURRENT_TIME")
    long countStoresOpenNow(Long id);
}
