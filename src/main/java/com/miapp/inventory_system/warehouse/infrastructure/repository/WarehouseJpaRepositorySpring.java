package com.miapp.inventory_system.warehouse.infrastructure.repository;

import com.miapp.inventory_system.warehouse.infrastructure.entity.WarehouseJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WarehouseJpaRepositorySpring extends JpaRepository<WarehouseJpaEntity, Long> {

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);

    List<WarehouseJpaEntity> findByActiveTrue();
}
