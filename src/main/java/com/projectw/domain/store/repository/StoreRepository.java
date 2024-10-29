package com.projectw.domain.store.repository;

import com.projectw.domain.store.entity.Store;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StoreRepository extends JpaRepository<Store, Long>, StoreDslRepository{
    @Query("SELECT COUNT(s) FROM Store s " +
            "WHERE s.id = :id AND " +
            "((s.openTime <= CURRENT_TIME AND s.closeTime >= CURRENT_TIME AND s.closeTime > s.openTime) " +  // 같은 날에 문을 닫는 경우
            "OR (s.openTime <= CURRENT_TIME OR s.closeTime >= CURRENT_TIME AND s.closeTime < s.openTime))")  // 다음 날까지 문을 여는 경우
    long countStoresOpenNow(Long id);

    @Query("SELECT s FROM Store s JOIN FETCH s.user WHERE s.id=:storeId")
    Optional<Store> findWithUserById(@Param("storeId") long storeId);

    @Query("SELECT s FROM Store s WHERE s.title LIKE %:title% ")
    Page<Store> findAllByTitle(Pageable pageable, String title);

    List<Store> findByDescription(String description); // 특정 카테고리에 속하는 음식점들

    List<Store> findByDescriptionAndAddressStartingWith(String description, String addressStart); // 카테고리와 특정 지역에 맞는 음식점
}
