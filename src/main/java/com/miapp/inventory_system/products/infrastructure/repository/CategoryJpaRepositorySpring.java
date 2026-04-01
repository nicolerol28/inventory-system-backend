package com.miapp.inventory_system.products.infrastructure.repository;

import com.miapp.inventory_system.products.infrastructure.entity.CategoryJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CategoryJpaRepositorySpring extends JpaRepository<CategoryJpaEntity, Long> {

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);

    @Query("SELECT c FROM CategoryJpaEntity c WHERE c.active = true")
    List<CategoryJpaEntity> findAllActive();

    Page<CategoryJpaEntity> findByActiveTrue(Pageable pageable);

    Page<CategoryJpaEntity> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<CategoryJpaEntity> findByActiveTrueAndNameContainingIgnoreCase(String name, Pageable pageable);
}
