package com.miapp.inventory_system.inventory.infrastructure.mapper;

import com.miapp.inventory_system.inventory.domain.model.Stock;
import com.miapp.inventory_system.inventory.infrastructure.entity.StockJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class StockMapper {

    public Stock toDomain(StockJpaEntity entity) {
        return Stock.reconstitute(
                entity.getId(),
                entity.getProductId(),
                entity.getWarehouseId(),
                entity.getQuantity(),
                entity.getMinQuantity(),
                entity.getUpdatedAt()
        );
    }

    public StockJpaEntity toEntity(Stock domain) {
        StockJpaEntity entity = new StockJpaEntity();
        entity.setId(domain.getId());
        entity.setProductId(domain.getProductId());
        entity.setWarehouseId(domain.getWarehouseId());
        entity.setQuantity(domain.getQuantity());
        entity.setMinQuantity(domain.getMinQuantity());
        entity.setUpdatedAt(domain.getUpdatedAt());
        return entity;
    }
}