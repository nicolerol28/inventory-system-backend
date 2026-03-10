package com.miapp.inventory_system.products.infrastructure.mapper;

import com.miapp.inventory_system.products.domain.model.Unit;
import com.miapp.inventory_system.products.infrastructure.entity.UnitJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class UnitMapper {

    public Unit toDomain(UnitJpaEntity entity) {
        return Unit.reconstitute(
                entity.getId(),
                entity.getName(),
                entity.getSymbol(),
                entity.isActive(),
                entity.getCreatedAt()
        );
    }

    public UnitJpaEntity toEntity(Unit domain) {
        UnitJpaEntity entity = new UnitJpaEntity();

        // Al momento de crear una nueva entidad el id debe ser null, pero si el
        // dominio ya tiene un ID (por ejemplo al actualizar) lo asignamos a la entidad
        if (domain.getId() != null) {
            entity.setId(domain.getId());
        }

        entity.setName(domain.getName());
        entity.setSymbol(domain.getSymbol());
        entity.setActive(domain.isActive());
        entity.setCreatedAt(domain.getCreatedAt());

        return entity;
    }
}