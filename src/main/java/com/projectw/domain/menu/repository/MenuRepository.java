package com.projectw.domain.menu.repository;

import com.projectw.domain.menu.entity.Menu;
import com.projectw.domain.store.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MenuRepository extends JpaRepository<Menu, Long> {
    List<Menu> findAllByStoreAndIsDeletedFalse(Store store);
    Optional<Menu> findByIdAndStoreId(Long menuId, Long storeId);
}
