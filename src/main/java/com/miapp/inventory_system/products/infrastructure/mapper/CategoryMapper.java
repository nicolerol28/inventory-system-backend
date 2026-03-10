package com.miapp.inventory_system.products.infrastructure.mapper;

import com.miapp.inventory_system.products.domain.model.Category;
import com.miapp.inventory_system.products.infrastructure.entity.CategoryJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public Category toDomain(CategoryJpaEntity entity){
        return Category.reconstitute(
                entity.getId(),
                entity.getName(),
                entity.isActive(),
                entity.getCreatedAt()
        );
    }

    public CategoryJpaEntity toEntity(Category domain){
        CategoryJpaEntity entity = new CategoryJpaEntity();

        if (domain.getId() != null) {
            entity.setId(domain.getId());
        }

        entity.setName(domain.getName());
        entity.setActive(domain.isActive());
        entity.setCreatedAt(domain.getCreatedAt());
        return entity;
    }
}
