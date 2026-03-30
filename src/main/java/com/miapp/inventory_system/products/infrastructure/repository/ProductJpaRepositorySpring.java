package com.miapp.inventory_system.products.infrastructure.repository;

import com.miapp.inventory_system.products.infrastructure.entity.ProductJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductJpaRepositorySpring extends JpaRepository<ProductJpaEntity, Long> {

     boolean existsByName(String name);

     boolean existsBySku(String sku);

     boolean existsByNameAndIdNot(String name, Long id);

     boolean existsBySkuAndIdNot(String sku, Long id);

    boolean existsByActiveTrueAndUnitId(Long unitId);

    boolean existsByActiveTrueAndCategoryId(Long categoryId);

    Page<ProductJpaEntity> findByActiveTrue(Pageable pageable);

    boolean existsByActiveTrueAndSupplierId(Long supplierId);
}
