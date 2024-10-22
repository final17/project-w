package com.projectw.domain.store.repository;

import com.projectw.domain.store.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreRepository extends JpaRepository<Store, Long>, StoreDslRepository{
}
