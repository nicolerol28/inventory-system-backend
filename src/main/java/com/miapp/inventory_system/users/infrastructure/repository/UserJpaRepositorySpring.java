package com.miapp.inventory_system.users.infrastructure.repository;

import com.miapp.inventory_system.users.infrastructure.entity.UserJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserJpaRepositorySpring extends JpaRepository<UserJpaEntity, Long> {

    java.util.Optional<UserJpaEntity> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);

    List<UserJpaEntity> findByActiveTrue();
}