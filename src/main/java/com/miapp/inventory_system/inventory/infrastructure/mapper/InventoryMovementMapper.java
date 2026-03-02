package com.miapp.inventory_system.inventory.infrastructure.mapper;

import com.miapp.inventory_system.inventory.domain.model.InventoryMovement;
import com.miapp.inventory_system.inventory.infrastructure.entity.InventoryMovementJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class InventoryMovementMapper {

    public InventoryMovement toDomain(InventoryMovementJpaEntity entity) {
        return InventoryMovement.reconstitute(
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

    public InventoryMovementJpaEntity toEntity(InventoryMovement domain) {
        InventoryMovementJpaEntity entity = new InventoryMovementJpaEntity();
        entity.setId(domain.getId());
        entity.setProductId(domain.getProductId());
        entity.setWarehouseId(domain.getWarehouseId());
        entity.setSupplierId(domain.getSupplierId());
        entity.setRegisteredBy(domain.getRegisteredBy());
        entity.setMovementType(domain.getMovementType());
        entity.setQuantity(domain.getQuantity());
        entity.setQuantityBefore(domain.getQuantityBefore());
        entity.setQuantityAfter(domain.getQuantityAfter());
        entity.setComment(domain.getComment());
        entity.setCreatedAt(domain.getCreatedAt());
        return entity;
    }


}