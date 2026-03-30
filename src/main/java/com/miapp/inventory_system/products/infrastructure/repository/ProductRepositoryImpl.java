package com.miapp.inventory_system.products.infrastructure.repository;

import com.miapp.inventory_system.products.domain.model.Product;
import com.miapp.inventory_system.products.domain.repository.ProductRepository;
import com.miapp.inventory_system.products.infrastructure.entity.ProductJpaEntity;
import com.miapp.inventory_system.products.infrastructure.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepository {

    private final ProductJpaRepositorySpring jpaRepository;
    private final ProductMapper mapper;

    @Override
    public Product save(Product product) {
        ProductJpaEntity entity = mapper.toEntity(product);
        ProductJpaEntity saved  = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Product> findById(Long id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public boolean existsByName(String name) {
        return jpaRepository.existsByName(name);
    }

    @Override
    public boolean existsBySku(String sku) {
        return jpaRepository.existsBySku(sku);
    }

    @Override
    public boolean existsByNameAndIdNot(String name, Long id) {
        return jpaRepository.existsByNameAndIdNot(name, id);
    }

    @Override
    public boolean existsBySkuAndIdNot(String sku, Long id) {
        return jpaRepository.existsBySkuAndIdNot(sku, id);
    }

    @Override
    public boolean existsActiveByUnitId(Long unitId) {
        return jpaRepository.existsByActiveTrueAndUnitId(unitId);
    }

    @Override
    public boolean existsActiveByCategoryId(Long categoryId) {
        return jpaRepository.existsByActiveTrueAndCategoryId(categoryId);
    }

    @Override
    public boolean existsActiveBySupplierId(Long supplierId) {
        return jpaRepository.existsByActiveTrueAndSupplierId(supplierId);
    }
}
