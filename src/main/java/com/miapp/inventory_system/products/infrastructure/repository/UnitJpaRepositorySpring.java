package com.miapp.inventory_system.products.infrastructure.repository;

import com.miapp.inventory_system.products.infrastructure.entity.UnitJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UnitJpaRepositorySpring extends JpaRepository<UnitJpaEntity, Long> {

    boolean existsByName(String name);

    boolean existsBySymbol(String symbol);

    boolean existsByNameAndIdNot(String name, Long id);

    boolean existsBySymbolAndIdNot(String symbol, Long id);

    @Query("SELECT u FROM UnitJpaEntity u WHERE u.active = true")
    List<UnitJpaEntity> findAllActive();

    Page<UnitJpaEntity> findByActiveTrue(Pageable pageable);

    Page<UnitJpaEntity> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<UnitJpaEntity> findByActiveTrueAndNameContainingIgnoreCase(String name, Pageable pageable);
}
