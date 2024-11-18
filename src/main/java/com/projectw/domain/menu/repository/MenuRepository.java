package com.projectw.domain.menu.repository;

import com.projectw.domain.menu.entity.Menu;
import com.projectw.domain.store.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MenuRepository extends JpaRepository<Menu, Long> {

    // 특정 Store에 속하고 삭제되지 않은 메뉴
    List<Menu> findAllByStoreAndIsDeletedFalse(Store store);
    Optional<Menu> findByIdAndStoreId(Long menuId, Long storeId);

    @Query("SELECT m FROM Menu m WHERE m.id IN(:menuIds)")
    List<Menu> getMenus(List<Long> menuIds);

    @Query("SELECT m FROM Menu m WHERE m.store.id = :id AND m.isDeleted=false")
    List<Menu> findAllByStoreId(@Param("id") Long id);
}
