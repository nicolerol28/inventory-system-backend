package com.miapp.inventory_system.suppliers.infrastructure.mapper;

import com.miapp.inventory_system.suppliers.domain.model.Supplier;
import com.miapp.inventory_system.suppliers.infrastructure.entity.SupplierJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class SupplierMapper {

    public Supplier toDomain(SupplierJpaEntity entity) {
        return Supplier.reconstitute(
                entity.getId(),
                entity.getName(),
                entity.getContact(),
                entity.getPhone(),
                entity.isActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public SupplierJpaEntity toEntity(Supplier domain) {
        SupplierJpaEntity entity = new SupplierJpaEntity();
        if (domain.getId() != null) {
            entity.setId(domain.getId());
        }
        entity.setName(domain.getName());
        entity.setContact(domain.getContact());
        entity.setPhone(domain.getPhone());
        entity.setActive(domain.isActive());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        return entity;
    }
}
