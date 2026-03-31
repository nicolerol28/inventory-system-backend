package com.miapp.inventory_system.inventory.infrastructure.repository;

import com.miapp.inventory_system.inventory.infrastructure.entity.StockJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface StockJpaRepositorySpring extends JpaRepository<StockJpaEntity, Long> {

    Optional<StockJpaEntity> findByProductIdAndWarehouseId(
            Long productId,
            Long warehouseId);

    Page<StockJpaEntity> findByWarehouseId(
            Long warehouseId,
            Pageable pageable);

    boolean existsByWarehouseId(Long warehouseId);

    boolean existsByProductId(Long productId);

    // SQL nativo para evitar acoplamiento entre JPA entities de módulos distintos.
    // StockJpaEntity (inventory) y products (tabla) se relacionan solo a nivel de base de datos.
    @Query(value = """
    SELECT s.* FROM stock s
    JOIN products p ON s.product_id = p.id
    WHERE s.warehouse_id = :warehouseId
    AND LOWER(p.name) LIKE LOWER(CONCAT('%', :productName, '%'))
    """,
            countQuery = """
    SELECT COUNT(*) FROM stock s
    JOIN products p ON s.product_id = p.id
    WHERE s.warehouse_id = :warehouseId
    AND LOWER(p.name) LIKE LOWER(CONCAT('%', :productName, '%'))
    """,
            nativeQuery = true)
    Page<StockJpaEntity> findByWarehouseIdAndProductNameContaining(
            @Param("warehouseId") Long warehouseId,
            @Param("productName") String productName,
            Pageable pageable);
}