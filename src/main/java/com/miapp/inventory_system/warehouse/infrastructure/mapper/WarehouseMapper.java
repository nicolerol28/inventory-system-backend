package com.miapp.inventory_system.warehouse.infrastructure.mapper;

import com.miapp.inventory_system.warehouse.domain.model.Warehouse;
import com.miapp.inventory_system.warehouse.infrastructure.entity.WarehouseJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class WarehouseMapper {

    public Warehouse toDomain(WarehouseJpaEntity entity) {
        return Warehouse.reconstitute(
                entity.getId(),
                entity.getName(),
                entity.getLocation(),
                entity.isActive(),
                entity.getCreatedAt()
        );
    }

    public WarehouseJpaEntity toEntity(Warehouse domain) {
        WarehouseJpaEntity entity = new WarehouseJpaEntity();
        if (domain.getId() != null) {
            entity.setId(domain.getId());
        }
        entity.setName(domain.getName());
        entity.setLocation(domain.getLocation());
        entity.setActive(domain.isActive());
        entity.setCreatedAt(domain.getCreatedAt());
        return entity;
    }
}
