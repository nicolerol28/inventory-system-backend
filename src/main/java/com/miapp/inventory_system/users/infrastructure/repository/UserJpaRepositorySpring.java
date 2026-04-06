package com.miapp.inventory_system.users.infrastructure.repository;

import com.miapp.inventory_system.users.infrastructure.entity.UserJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserJpaRepositorySpring extends JpaRepository<UserJpaEntity, Long> {

    Optional<UserJpaEntity> findByEmail(String email);

    Optional<UserJpaEntity> findByGoogleId(String googleId);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);

    @Query("SELECT u FROM UserJpaEntity u WHERE u.active = true")
    List<UserJpaEntity> findAllActive();

    Page<UserJpaEntity> findByActiveTrue(Pageable pageable);

    Page<UserJpaEntity> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<UserJpaEntity> findByActiveTrueAndNameContainingIgnoreCase(String name, Pageable pageable);
}
