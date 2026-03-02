package com.miapp.inventory_system.inventory.infrastructure.repository;

import com.miapp.inventory_system.inventory.infrastructure.entity.InventoryMovementJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryMovementJpaRepositorySpring
        extends JpaRepository<InventoryMovementJpaEntity, Long> {
}