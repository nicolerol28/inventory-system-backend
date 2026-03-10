package com.miapp.inventory_system.products.domain.repository;

import com.miapp.inventory_system.products.domain.model.Category;

import java.util.Optional;

public interface CategoryRepository {

    Category save(Category category);

    Optional<Category> findById(Long id);

    boolean existsByName(String name);

    // Existe este nombre en otra Category diferente a la mia?
    boolean existsByNameAndIdNot(String name, Long id);
}
