package com.miapp.inventory_system.warehouse.infrastructure.repository;

import com.miapp.inventory_system.warehouse.infrastructure.entity.WarehouseJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface WarehouseJpaRepositorySpring extends JpaRepository<WarehouseJpaEntity, Long> {

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);

    @Query("SELECT w FROM WarehouseJpaEntity w WHERE w.active = true")
    List<WarehouseJpaEntity> findAllActive();

    Page<WarehouseJpaEntity> findByActiveTrue(Pageable pageable);

    Page<WarehouseJpaEntity> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<WarehouseJpaEntity> findByActiveTrueAndNameContainingIgnoreCase(String name, Pageable pageable);
}
