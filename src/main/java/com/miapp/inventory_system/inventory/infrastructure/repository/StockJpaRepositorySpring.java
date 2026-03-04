package com.miapp.inventory_system.inventory.infrastructure.repository;

import com.miapp.inventory_system.inventory.infrastructure.entity.StockJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StockJpaRepositorySpring extends JpaRepository<StockJpaEntity, Long> {

    Optional<StockJpaEntity> findByProductIdAndWarehouseId(
            Long productId,
            Long warehouseId);

    Page<StockJpaEntity> findByWarehouseId(
            Long warehouseId,
            Pageable pageable);

}