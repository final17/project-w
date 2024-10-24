package com.projectw.domain.menu.repository;

import com.projectw.domain.menu.entity.Menu;
import com.projectw.domain.store.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuRepository extends JpaRepository<Menu, Long> {

    // 특정 Store에 속하고 삭제되지 않은 메뉴 목록 조회
    List<Menu> findAllByStoreAndIsDeletedFalse(Store store);
}
