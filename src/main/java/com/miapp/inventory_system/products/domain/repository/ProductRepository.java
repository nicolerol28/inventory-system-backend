package com.miapp.inventory_system.products.domain.repository;

import com.miapp.inventory_system.products.domain.model.Product;

import java.util.Optional;

public interface ProductRepository {

    Product save(Product product);

    Optional<Product> findById(Long id);

    boolean existsByName(String name);

    boolean existsBySku(String sku);

    // Existe este nombre en otro Product diferente al mio?
    boolean existsByNameAndIdNot(String name, Long id);

    // Existe este sku en otro Product diferente al mio?
    boolean existsBySkuAndIdNot(String sku, Long id);

    boolean existsActiveByUnitId(Long unitId);

    boolean existsActiveByCategoryId(Long categoryId);

    boolean existsActiveBySupplierId(Long supplierId);
}
