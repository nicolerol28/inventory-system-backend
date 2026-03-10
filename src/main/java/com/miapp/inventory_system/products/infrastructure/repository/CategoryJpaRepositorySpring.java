package com.miapp.inventory_system.products.infrastructure.repository;

import com.miapp.inventory_system.products.infrastructure.entity.CategoryJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryJpaRepositorySpring extends JpaRepository<CategoryJpaEntity, Long> {

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);

    List<CategoryJpaEntity> findByActiveTrue();
}
