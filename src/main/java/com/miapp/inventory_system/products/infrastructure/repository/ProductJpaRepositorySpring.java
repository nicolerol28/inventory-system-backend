package com.miapp.inventory_system.products.infrastructure.repository;

import com.miapp.inventory_system.products.infrastructure.entity.ProductJpaEntity;
import com.miapp.inventory_system.products.infrastructure.entity.UnitJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductJpaRepositorySpring extends JpaRepository<ProductJpaEntity, Long> {

     boolean existsByName(String name);

     boolean existsBySku(String sku);

     boolean existsByNameAndIdNot(String name, Long id);

     boolean existsBySkuAndIdNot(String sku, Long id);

    boolean existsByActiveTrueAndUnitId(Long unitId);

    boolean existsByActiveTrueAndCategoryId(Long categoryId);

    List<ProductJpaEntity> findByActiveTrue();
}
