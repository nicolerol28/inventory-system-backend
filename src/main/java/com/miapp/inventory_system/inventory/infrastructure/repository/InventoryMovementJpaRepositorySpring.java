package com.miapp.inventory_system.inventory.infrastructure.repository;

import com.miapp.inventory_system.inventory.domain.model.MovementType;
import com.miapp.inventory_system.inventory.infrastructure.entity.InventoryMovementJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface InventoryMovementJpaRepositorySpring
        extends JpaRepository<InventoryMovementJpaEntity, Long> {

    Page<InventoryMovementJpaEntity> findByProductIdAndWarehouseId(
            Long productId,
            Long warehouseId,
            Pageable pageable);

    Page<InventoryMovementJpaEntity> findByWarehouseId(
            Long warehouseId,
            Pageable pageable);

    Page<InventoryMovementJpaEntity> findByWarehouseIdAndMovementType(
            Long warehouseId,
            MovementType movementType,
            Pageable pageable);

    Page<InventoryMovementJpaEntity> findByWarehouseIdAndCreatedAtBetween(
            Long warehouseId,
            LocalDateTime from,
            LocalDateTime to,
            Pageable pageable);
}