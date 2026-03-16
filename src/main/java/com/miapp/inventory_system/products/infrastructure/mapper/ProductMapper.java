package com.miapp.inventory_system.products.infrastructure.mapper;

import com.miapp.inventory_system.products.domain.model.Product;
import com.miapp.inventory_system.products.infrastructure.entity.ProductJpaEntity;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ProductMapper {

    public Product toDomain(ProductJpaEntity entity) {
        return Product.reconstitute(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getSku(),
                entity.getUnitId(),
                entity.getCategoryId(),
                entity.getSupplierId(),
                Optional.ofNullable(entity.getPurchasePrice()),
                Optional.ofNullable(entity.getSalePrice()),
                entity.isActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public ProductJpaEntity toEntity(Product domain) {
        ProductJpaEntity entity = new ProductJpaEntity();

        // Si el dominio ya tiene un ID (por ejemplo al actualizar) lo asignamos a la entidad
        if (domain.getId() != null) {
            entity.setId(domain.getId());
        }

        entity.setName(domain.getName());
        entity.setDescription(domain.getDescription());
        entity.setSku(domain.getSku());
        entity.setUnitId(domain.getUnitId());
        entity.setCategoryId(domain.getCategoryId());
        entity.setSupplierId(domain.getSupplierId());
        entity.setPurchasePrice(domain.getPurchasePrice().orElse(null));
        entity.setSalePrice(domain.getSalePrice().orElse(null));
        entity.setActive(domain.isActive());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());

        return entity;
    }
}
