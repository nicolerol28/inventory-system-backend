package com.miapp.inventory_system.products.infrastructure.repository;

import com.miapp.inventory_system.products.infrastructure.entity.ProductJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductJpaRepositorySpring extends JpaRepository<ProductJpaEntity, Long> {

     boolean existsByName(String name);

     boolean existsBySku(String sku);

     boolean existsByNameAndIdNot(String name, Long id);

     boolean existsBySkuAndIdNot(String sku, Long id);

    boolean existsByActiveTrueAndUnitId(Long unitId);

    boolean existsByActiveTrueAndCategoryId(Long categoryId);

    Page<ProductJpaEntity> findByActiveTrue(Pageable pageable);

    boolean existsByActiveTrueAndSupplierId(Long supplierId);

    @Query(value = """
    SELECT * FROM products p
    WHERE (:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')))
    AND (:categoryId IS NULL OR p.category_id = :categoryId)
    AND (:unitId IS NULL OR p.unit_id = :unitId)
    """,
            countQuery = """
    SELECT COUNT(*) FROM products p
    WHERE (:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')))
    AND (:categoryId IS NULL OR p.category_id = :categoryId)
    AND (:unitId IS NULL OR p.unit_id = :unitId)
    """,
            nativeQuery = true)
    Page<ProductJpaEntity> findByFilters(
            @Param("name") String name,
            @Param("categoryId") Long categoryId,
            @Param("unitId") Long unitId,
            Pageable pageable);

    @Query(value = """
    SELECT * FROM products p
    WHERE p.active = true
    AND (:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')))
    AND (:categoryId IS NULL OR p.category_id = :categoryId)
    AND (:unitId IS NULL OR p.unit_id = :unitId)
    """,
            countQuery = """
    SELECT COUNT(*) FROM products p
    WHERE p.active = true
    AND (:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')))
    AND (:categoryId IS NULL OR p.category_id = :categoryId)
    AND (:unitId IS NULL OR p.unit_id = :unitId)
    """,
            nativeQuery = true)
    Page<ProductJpaEntity> findByActiveTrueAndFilters(
            @Param("name") String name,
            @Param("categoryId") Long categoryId,
            @Param("unitId") Long unitId,
            Pageable pageable);
}
