package com.miapp.inventory_system.inventory.application.query;

import com.miapp.inventory_system.inventory.api.dto.InventoryMovementResponse;
import com.miapp.inventory_system.shared.dto.PageResponse;
import com.miapp.inventory_system.inventory.api.dto.StockResponse;
import com.miapp.inventory_system.inventory.infrastructure.entity.InventoryMovementJpaEntity;
import com.miapp.inventory_system.inventory.infrastructure.entity.StockJpaEntity;
import com.miapp.inventory_system.inventory.infrastructure.repository.InventoryMovementJpaRepositorySpring;
import com.miapp.inventory_system.inventory.infrastructure.repository.StockJpaRepositorySpring;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryQueryService {

    private final StockJpaRepositorySpring stockJpaRepository;
    private final InventoryMovementJpaRepositorySpring movementJpaRepository;

    // Consulta stock de un producto en un almacen especifico
    public StockResponse getStock(Long productId, Long warehouseId) {
        return stockJpaRepository
                .findByProductIdAndWarehouseId(productId, warehouseId)
                .map(this::toStockResponse)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No existe stock para el producto " + productId + " en este almacén"));
    }

    // Consulta stock paginado de todos los productos en un almacen
    public PageResponse<StockResponse> getStockByWarehouse(
            Long warehouseId, int page, int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        Page<StockJpaEntity> result =
                stockJpaRepository.findByWarehouseId(warehouseId, pageable);

        return toPageResponse(result, this::toStockResponse);
    }

    // Consulta historial de movimientos de un producto en un almacen
    public PageResponse<InventoryMovementResponse> getMovements(
            Long productId, Long warehouseId, int page, int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<InventoryMovementJpaEntity> result =
                movementJpaRepository.findByProductIdAndWarehouseId(
                        productId, warehouseId, pageable);

        return toPageResponse(result, this::toMovementResponse);
    }

    // Consulta movimientos paginados de un almacen completo
    public PageResponse<InventoryMovementResponse> getMovementsByWarehouse(
            Long warehouseId, int page, int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<InventoryMovementJpaEntity> result =
                movementJpaRepository.findByWarehouseId(warehouseId, pageable);

        return toPageResponse(result, this::toMovementResponse);
    }

    // Consulta movimientos paginados por rango de fechas
    public PageResponse<InventoryMovementResponse> getMovementsByDateRange(
            Long warehouseId, LocalDateTime from, LocalDateTime to,
            int page, int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<InventoryMovementJpaEntity> result =
                movementJpaRepository.findByWarehouseIdAndCreatedAtBetween(
                        warehouseId, from, to, pageable);

        return toPageResponse(result, this::toMovementResponse);
    }


    // Metodos privados de conversion

    private StockResponse toStockResponse(StockJpaEntity entity) {
        return new StockResponse(
                entity.getId(),
                entity.getProductId(),
                entity.getWarehouseId(),
                entity.getQuantity(),
                entity.getMinQuantity(),
                entity.getQuantity().compareTo(entity.getMinQuantity()) < 0,
                entity.getUpdatedAt()
        );
    }

    private InventoryMovementResponse toMovementResponse(InventoryMovementJpaEntity entity) {
        return new InventoryMovementResponse(
                entity.getId(),
                entity.getProductId(),
                entity.getWarehouseId(),
                entity.getSupplierId(),
                entity.getRegisteredBy(),
                entity.getMovementType(),
                entity.getQuantity(),
                entity.getQuantityBefore(),
                entity.getQuantityAfter(),
                entity.getComment(),
                entity.getCreatedAt()
        );
    }

    private <E, R> PageResponse<R> toPageResponse(
            Page<E> page,
            java.util.function.Function<E, R> mapper) {

        List<R> content = page.getContent()
                .stream()
                .map(mapper)
                .toList();

        return new PageResponse<>(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }
}