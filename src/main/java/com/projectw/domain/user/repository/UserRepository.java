package com.projectw.domain.user.repository;

import com.projectw.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> , UserDslRepository {

    @Query("SELECT u FROM User u WHERE u.email=:email")
    Optional<User> findByEmail(@Param("email") String email);

    @Query("SELECT u FROM User u WHERE u.nickname=:nickname")
    Optional<User> findByNickname(@Param("nickname") String nickname);

    boolean existsByNickname(String nickname);

    boolean existsByEmail(String email);

    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.id = :id AND u.isDeleted = true")
    boolean isDeletedUser(@Param("id") String userId);
}
