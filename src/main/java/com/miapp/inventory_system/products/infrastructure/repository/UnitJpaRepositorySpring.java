package com.miapp.inventory_system.products.infrastructure.repository;

import com.miapp.inventory_system.products.infrastructure.entity.UnitJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UnitJpaRepositorySpring extends JpaRepository<UnitJpaEntity, Long> {

    boolean existsByName(String name);

    boolean existsBySymbol(String symbol);

    boolean existsByNameAndIdNot(String name, Long id);

    boolean existsBySymbolAndIdNot(String symbol, Long id);

    List<UnitJpaEntity> findByActiveTrue();
}